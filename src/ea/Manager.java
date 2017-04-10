/*
 * Engine Alpha ist eine anfängerorientierte 2D-Gaming Engine.
 *
 * Copyright (c) 2011 - 2014 Michael Andonie and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ea;

import ea.internal.util.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Der Manager ist eine Standardklasse und eine der wichtigsten der Engine Alpha, die zur
 * Interaktion ausserhalb der engine benutzt werden kann.<br /> Neben einer Liste aller moeglichen
 * Fonts handelt er auch das <b>Ticker-System</b>. Dies ist ein relativ konsistentes System, das
 * viele <b><code>Ticker</code></b>-Objekte - Interfaces mit einer Methode, die in immergleichen
 * Abstaenden immer wieder aufgerufen werden. <br /> <br />
 * <p/>
 * Bewusst leitet sich diese Klasse nicht von <code>Thread</code> ab. Hierdurch kann ein Manager
 * ohne grossen Ressourcenaufwand erstellt werden, wobei der Thread (und damit Computerrechenzeit)
 * erst mit dem aktiven Nutzen erstellt wird
 *
 * @author Michael Andonie, Niklas Keller <me@kelunik.com>
 * @see Ticker
 */
public class Manager {
	/**
	 * Der Standard-Manager. Dieser wird nur innerhalb des "ea"-Paketes-verwendet!<br /> Er ist der
	 * Manager, der verschiedene Ticker-Bedürfnisse von einzelnen internen Klassen deckt und seine
	 * Fassung ist exakt an der Anzahl der noetigen Ticker angeglichen. Dieser ist fuer:<br /> - Die
	 * Fensterkontrollroutine<br /> - Die Kollisionskontrollroutine der Klasse
	 * <code>Physik</code><br /> - Die Figurenanimationsroutine<br /> - Die Leuchtanimationsroutine
	 */
	public static final Manager standard = new Manager("Interner Routinenmanager");

	/**
	 * Alle möglichen Fontnamen des Systems, auf dem man sich gerade befindet.<br /> Hiernach werden
	 * Überprüfungen gemacht, ob die gewünschte Schriftart auch auf dem hiesigen System vorhanden
	 * ist.
	 */
	public static final String[] fontNamen;

	static {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		fontNamen = ge.getAvailableFontFamilyNames();
	}

	/**
	 * Der Counter aller vorhandenen Manager-Tickerthreads
	 */
	private static int cnt = 0;

	/**
	 * Der Name des Threads, ueber dem dieser Manager arbeitet
	 */
	private final String name;

	private final ScheduledExecutorService executor;

	/**
	 * Die Liste aller Aufträge.
	 */
	private volatile ArrayList<Job> jobs = new ArrayList<>();

	/**
	 * Vereinfachter Konstruktor ohne Parameter.<br /> Bei einem normalen Spiel muss nicht extra ein
	 * Manager erstellt werden. Dafür gibt es bereits eine Referenz<br /> <br /> <code>public final
	 * Manager manager;</code><br /> <br /> in der Klass <code>Game</code> und damit auch in jeder
	 * spielsteurnden Klasse.
	 */
	public Manager () {
		this("Ticker " + (cnt + 1));
	}

	/**
	 * Konstruktor eines Managers.<br /> Bei einem normalen Spiel muss nicht extra ein Manager
	 * erstellt werden. Dafuer gibt es bereits eine Referenz<br /> <br /> <code>public final Manager
	 * manager;</code><br /> <br /> in der Klass <code>Game</code> und damit auch in jeder
	 * spielsteurnden Klasse.
	 *
	 * @param name
	 * 		Der Name, den der Thread haben wird, über den dieser Manager läuft.<br /> Dieser Parameter
	 * 		kann auch einfach weggelassen werden; in diesem Fall erhaelt der Ticker einen
	 * 		standardisierten Namen.
	 *
	 * @see #Manager()
	 */
	public Manager (String name) {
		this.name = name;
		this.executor = Executors.newScheduledThreadPool(10);

		cnt++;
	}

	/**
	 * Prüft, ob ein Font auf diesem Computer existiert.
	 *
	 * @param name
	 * 		Der Name des zu ueberpruefenden Fonts
	 *
	 * @return <code>true</code>, falls der Font auf dem System existiert, sonst <code>false</code>
	 */
	public static boolean fontExistiert (String name) {
		for (String s : fontNamen) {
			if (s.equals(name)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Diese Methode prüft, ob zur Zeit <b>mindestens 1 Ticker</b> an diesem Manager ausgeführt
	 * wird.
	 *
	 * @return <code>true</code>, wenn mindestens 1 Ticker an diesem Manager zur Zeit mit seiner
	 * <code>tick()</code>-Methode ausgeführt wird. Sonst <code>false</code>.
	 */
	public synchronized boolean hatAktiveTicker () {
		for (Iterator<Job> iterator = jobs.iterator(); iterator.hasNext();) {
			if (iterator.next().active) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Meldet einen Ticker am Manager an. Ab sofort läuft er auf diesem Manager und damit wird auch
	 * dessen <code>tick()</code>-Methode immer wieder aufgerufen.
	 *
	 * @param t
	 * 		Der anzumeldende Ticker
	 *
	 * @see #anmelden(Ticker)
	 */
	public synchronized void anmelden (Ticker t, int intervall) {
		anmelden(t);
		starten(t, intervall);
	}

	/**
	 * Macht diesem Manager einen Ticker bekannt, <b>OHNE</b> ihn aufzurufen.
	 *
	 * @param t
	 * 		Der anzumeldende Ticker
	 *
	 * @see #anmelden(Ticker, int)
	 */
	public synchronized void anmelden (Ticker t) {
		if (istAngemeldet(t)) {
			Logger.warning("Der Ticker ist bereits an diesem Manager angemeldet und wird nicht erneut angemeldet.");
			return;
		}

		jobs.add(new Job(t, 1000, false));

		if(EngineAlpha.isDebug()) {
			Logger.info("Ticker wurde angemeldet: " + t.toString());
		}
	}

	/**
	 * Startet einen Ticker, der <b>bereits an diesem Manager angemeldet ist</b>.<br /> Läuft der
	 * Ticker bereits, passiert gar nichts. War der Ticker nicht angemeldet, kommt eine
	 * Fehlermeldung.
	 *
	 * @param t
	 * 		Der zu startende, <b>bereits am <code>Manager</code> angemeldete</b> Ticker.
	 * @param intervall
	 * 		Das Intervall im ms<sup>-1</sup>, in dem dieser Ticker ab sofort immer wieder aufgerufen
	 * 		wird.
	 *
	 * @see #anhalten(Ticker)
	 */
	public synchronized void starten (final Ticker t, int intervall) {
		if (!istAngemeldet(t)) {
			Logger.error("Der Ticker ist noch nicht angemeldet.");
			return;
		}

		final Job job = getJob(t);

		if (job.active) {
			Logger.error("Ticker ist bereits am Laufen!");
			return;
		}

		Runnable r = new Runnable() {
			@Override
			public void run () {
				// Otherwise these exceptions don't show up anywhere!
				try {
					t.tick();
				} catch (RuntimeException e) {
					Logger.error("Kritischer Fehler im Ticker: Eine Ausnahme wurde nicht abgefangen. Der Ticker wurde angehalten. Die folgende StackTrace sollte dir weitere Informationen liefern.");
					e.printStackTrace();

					job.setActive(false);
					job.scheduledFuture.cancel(true);
				}
			}
		};

		ScheduledFuture<?> future = executor.scheduleAtFixedRate(r, intervall, intervall, TimeUnit.MILLISECONDS);

		job.setActive(true);
		job.setFuture(future);
	}

	/**
	 * Prüft, ob ein Ticker t bereits angemeldet ist.
	 *
	 * @param t
	 * 		Der zu prüfende Ticker.
	 *
	 * @return <code>true</code>, falls der Ticker bereits an diesem <code>Manager</code> angemeldet
	 * ist, sonst <code>false</code>.
	 */
	public synchronized boolean istAngemeldet (Ticker t) {
		for (Iterator<Job> iterator = jobs.iterator(); iterator.hasNext();) {
			Job currentJob = iterator.next();
			if (currentJob.steuert(t)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Gibt den Job zu einem bestimmten Ticker aus.
	 *
	 * @param t
	 * 		Der Ticker zu dem entsprechenden Job
	 */
	private synchronized Job getJob (final Ticker t) {
		for (Iterator<Job> iterator = jobs.iterator(); iterator.hasNext();) {
			Job currentJob = iterator.next();
			if (currentJob.steuert(t)) {
				return currentJob;
			}
		}

		return null;
	}

	/**
	 * Diese Methode setzt das Intervall eines Tickers neu.
	 *
	 * @param t
	 * 		Der Ticker, dessen Intervall geaendert werden soll.<br /> Ist er nicht an dem Manager
	 * 		angemeldet, so wird eine Fehlermeldung ausgeloest!
	 * @param intervall
	 * 		Das neue Intervall fuer den Ticker
	 */
	public synchronized void intervallSetzen (Ticker t, int intervall) {
		if (!istAngemeldet(t)) {
			Logger.error("Der Ticker ist noch nicht angemeldet.");
			return;
		}

		Job job = getJob(t);

		if (intervall == job.interval) {
			return;
		}

		if (job.active) {
			anhalten(t);
			starten(t, intervall);
		}

		job.interval = intervall;
	}

	/**
	 * Hält einen Ticker an, der <b>bereits an diesem Manager angemeldet ist</b>.<br /> Ist der
	 * Ticker bereits angehalten, passiert gar nichts. War der Ticker nicht angemeldet, kommt eine
	 * Fehlermeldung.
	 *
	 * @param t
	 * 		Der anzuhaltende Ticker
	 *
	 * @see #starten(Ticker, int)
	 */
	public synchronized void anhalten (Ticker t) {
		if (!istAngemeldet(t)) {
			Logger.error("Der Ticker ist noch nicht angemeldet.");
			return;
		}

		Job job = getJob(t);
		ScheduledFuture<?> future = job.scheduledFuture;

		future.cancel(false);
		job.setActive(false);

		if(EngineAlpha.isDebug()) {
			Logger.info("Ticker wurde angehalten: " + t.toString());
		}
	}

	/**
	 * Meldet einen Ticker ab.<br /> War dieser Ticker nicht angemeldet, so passiert nichts &ndash;
	 * außer einer Fehlermeldung.
	 *
	 * @param t
	 * 		abzumeldender Ticker
	 */
	public synchronized void abmelden (Ticker t) {
		if (!istAngemeldet(t)) {
			Logger.error("Der Ticker ist noch nicht angemeldet.");
			return;
		}

		Job job = getJob(t);

		if (job.active) {
			anhalten(t);
		}

		jobs.remove(job);

		if(EngineAlpha.isDebug()) {
			Logger.info("Ticker wurde abgemeldet: " + t.toString());
		}
	}

	/**
	 * Beendet den Thread, den dieser Manager verwendet und damit den Manager selbst. Sollte
	 * <b>nur</b> aufgerufen werden, wenn der Manager selbst gelöscht werden soll.
	 */
	public final void kill () {
		alleAbmelden();
	}

	/**
	 * Macht diesen Manager frei von allen aktiven Tickern, jedoch ohne ihn selbst zu beenden. Neue
	 * Ticker können jederzeit wieder angemeldet werden.
	 */
	public synchronized final void alleAbmelden () {
		for (Iterator<Job> iterator = jobs.iterator(); iterator.hasNext();) {
			Job currentJob = iterator.next();
			if (currentJob.isActive()) {
				anhalten(currentJob.getTicker());
			}
		}

		jobs = new ArrayList<>();
	}

	/**
	 * Diese Klasse beschreibt einen "Tick-Job" und sammelt so alle Eigenschaften:<br /> Ticker,
	 * Intervall, Aktivität.
	 */
	private final class Job {
		/**
		 * Der Ticker dieses Jobs
		 */
		private final Ticker ticker;

		/**
		 * Das Intervall dieses Jobs
		 */
		private int interval;

		/**
		 * Ob der Ticker momentan aktiv ist.
		 */
		private boolean active;

		/**
		 * ScheduledFuture, das aufgerufen wird.
		 */
		private ScheduledFuture<?> scheduledFuture;

		/**
		 * Konstruktor.
		 *
		 * @param ticker
		 * 		Ticker dieses Auftrages
		 * @param intervall
		 * 		Aufrufintervall des Tickers in ms<sup>-1</sup>
		 * @param aktiv
		 * 		Ob dieser Ticker aktiv ist
		 */
		public Job (Ticker ticker, int intervall, boolean aktiv) {
			this.ticker = ticker;
			this.interval = intervall;
			this.active = aktiv;
		}

		/**
		 * Prüft, ob dieser Job einen bestimmten Ticker steuert.
		 *
		 * @param t
		 * 		Ticker, der auf Gleichheit mit dem angelegten zu prüfen ist
		 *
		 * @return <code>true</code>, wenn beide Ticker identisch sind (Prüfung mit
		 * <code>equals</code>), sonst <code>false</code>.
		 */
		public final boolean steuert (final Ticker t) {
			return ticker.equals(t);
		}

		/**
		 * Setzt das Aufrufintervall neu.
		 *
		 * @param interval
		 * 		Neues Aufrufintervall
		 */
		public void setInterval (int interval) {
			this.interval = interval;
		}

		/**
		 * Gibt zurück, ob der Job aktiv ist.
		 *
		 * @return <code>true</code>, falls der Job gerade aktiv ist, sonst <code>false</code>.
		 */
		public boolean isActive () {
			return this.active;
		}

		/**
		 * Setzt, ob der anliegende Ticker momentan aktiv ist.
		 *
		 * @param active
		 * 		Ob der anliegende Ticker aufgerufen werden soll, oder nicht
		 */
		public void setActive (boolean active) {
			this.active = active;
		}

		/**
		 * Setzt den Task neu.
		 *
		 * @param future
		 * 		Neues tatsächliche ScheduledFuture, das ausgeführt wird
		 */
		public void setFuture (ScheduledFuture<?> future) {
			this.scheduledFuture = future;
		}

		/**
		 * Gibt den Ticker zurück, der zu diesem Job gehört.
		 *
		 * @return Ticker, der zu diesem Job gehört
		 */
		public Ticker getTicker () {
			return ticker;
		}
	}
}
