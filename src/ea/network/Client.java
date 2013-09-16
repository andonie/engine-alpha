/* Engine Alpha ist eine anfaengerorientierte 2D-Gaming Engine.
 *
 * Copyright (C) 2011  Michael Andonie
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package ea.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Diese Klasse ermoeglicht das Aufbauen einer Client-Verbindung zu einem
 * Server.
 * @author Andonie
 */
public class Client 
extends Thread 
implements Empfaenger, SenderInterface {
	
	/**
	 * Der socket, ueber den die Verbindung aufgebaut wird.
	 */
	private Socket socket;
	
	/**
	 * Die gew�nschte Ziel-IP-Adresse des Socket
	 */
	private final String ipAdresse;
	
	/**
	 * Der Name, mit dem sich der Client beim
	 * Server vorstellt.
	 */
	private final String name;
	
	/**
	 * Der Port des Socket.
	 */
	private final int port;
	
	/**
	 * Diese Verbindung ist ungleich null, sobald die Verbindung mit dem
	 * Server aufgebaut wurde.
	 */
	private NetzwerkVerbindung verbindung;
	
	public Client(String name, String ipAdresse, int port) {
		this.setDaemon(true);
		this.name = name;
		this.ipAdresse = ipAdresse;
		this.port = port;
		start();
	}
	
	public Client(String ipAdresse, int port) {
		this("Unbenannter Client", ipAdresse, port);
	}
	
	/**
	 * Die run-Methode des Threads baut eine Verbindung zum Server aus.
	 * Sobald dieser Thread erfolgreich abgeschlossen ist, kann die Verbindung 
	 * zur Kommunikation genutzt werden.
	 */
	@Override
	public void run() {
		try {
			socket = new Socket(ipAdresse, port);
			
			//Stelle sicher, dass der Socket auch wieder geschlossen wird.
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					verbindungSchliessen();
				}
			});
			
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream()));
			InputStream is = socket.getInputStream();
			
			bw.write("xe"+name);
			bw.newLine();
			bw.flush();
			
			//Set up interpreter
			NetzwerkInterpreter interpreter = new NetzwerkInterpreter(
					new BufferedReader(new InputStreamReader(is)));
			interpreter.empfaengerHinzufuegen(this);
			
			NetzwerkVerbindung vb = new NetzwerkVerbindung(
					name, bw, interpreter);
			verbindung = vb;
			synchronized(this) {
				this.notifyAll();
			}
		} catch (UnknownHostException e) {
			System.err.println("Konnte die IP-Adresse nicht zuordnen...");
		} catch (IOException e) {
			System.err.println("Es gab Input/Output - Schwierigkeiten. Sind ausreichende Rechte fuer"
					+ " Internet etc. vorhanden? Das System k�nnte die Netzwerkanfrage ablehnen.");
		}
	}
	
	/**
	 * Diese Methode <b>stellt sicher</b>, dass eine Verbindung mit dem Server besteht.<br />
	 * Diese Methode friert den ausf�hrenden Thread ein, wenn noch keine Verbindung besteht
	 * und endet erst, wenn die Verbindung aufgebaut wurde.
	 */
	public void warteAufVerbindung() {
		if(verbindung == null) {
			synchronized(this) {
				try {
					wait();
				} catch (InterruptedException e) {
					System.err.println("Achtung. Es k�nnte trotz warteAufVerbindung() noch "
							+ "keine Verbindung bestehen, da der Warteprozess unterbrochen wurde.");
				}
			}
		}
	}
	
	public void verbindungSchliessen() {
		if(!socket.isClosed()) {
			verbindung.beendeVerbindung();
			try {
				socket.close();
			} catch(IOException e) {
				System.err.println("Konnte den Verbindungs-Socket nicht mehr schliessen.");
			}
		}
	}
	
	/**
	 * Setzt den Empfaenger, der ueber jede Nachricht an diesen 
	 * Client informiert wird.
	 * @param e	Der Empfaenger, and den alle Nachrichten an 
	 * diesen Client weitergereicht werden sollen.
	 */
	public void empfaengerHinzufuegen(Empfaenger e) {
		warteAufVerbindung();
		this.verbindung.getInterpreter().empfaengerHinzufuegen(e);
	}
	
	/**
	 * {@inheritDoc}
	 * Sendet, sofern die Verbindung zum Server bereits aufgebaut wurde.
	 * Sonst passiert <b>wird solange gewartet, bis der Client sich
	 * mit einem Server verbinden konnte.</b>.
	 */
	@Override
	public void sendeString(String string) {
		warteAufVerbindung();
		verbindung.sendeString(string);
	}
	
	/**
	 * {@inheritDoc}
	 * Sendet, sofern die Verbindung zum Server bereits aufgebaut wurde.
	 * Sonst passiert <b>wird solange gewartet, bis der Client sich
	 * mit einem Server verbinden konnte.</b>.
	 */
	@Override
	public void sendeInt(int i) {
		warteAufVerbindung();
		verbindung.sendeInt(i);
	}
	
	/**
	 * {@inheritDoc}
	 * Sendet, sofern die Verbindung zum Server bereits aufgebaut wurde.
	 * Sonst passiert <b>wird solange gewartet, bis der Client sich
	 * mit einem Server verbinden konnte.</b>.
	 */
	@Override
	public void sendeByte(byte b) {
		warteAufVerbindung();
		verbindung.sendeByte(b);
	}
	
	/**
	 * {@inheritDoc}
	 * Sendet, sofern die Verbindung zum Server bereits aufgebaut wurde.
	 * Sonst passiert <b>wird solange gewartet, bis der Client sich
	 * mit einem Server verbinden konnte.</b>.
	 */
	@Override
	public void sendeDouble(double d) {
		warteAufVerbindung();
		verbindung.sendeDouble(d);
	}
	
	/**
	 * {@inheritDoc}
	 * Sendet, sofern die Verbindung zum Server bereits aufgebaut wurde.
	 * Sonst passiert <b>wird solange gewartet, bis der Client sich
	 * mit einem Server verbinden konnte.</b>.
	 */
	@Override
	public void sendeChar(char c) {
		warteAufVerbindung();
		verbindung.sendeChar(c);
	}
	
	/**
	 * {@inheritDoc}
	 * Sendet, sofern die Verbindung zum Server bereits aufgebaut wurde.
	 * Sonst passiert <b>wird solange gewartet, bis der Client sich
	 * mit einem Server verbinden konnte.</b>.
	 */
	@Override
	public void sendeBoolean(boolean b) {
		warteAufVerbindung();
		verbindung.sendeBoolean(b);
	}
	
	/**
	 * {@inheritDoc}
	 * Sendet, sofern die Verbindung zum Server bereits aufgebaut wurde.
	 * Sonst passiert <b>wird solange gewartet, bis der Client sich
	 * mit einem Server verbinden konnte.</b>.
	 */
	@Override
	public void beendeVerbindung() {
		warteAufVerbindung();
		if(!verbindung.istAktiv()) {
			System.err.println("Die Verbindung zum Server wurde "
					+ "bereits beendet.");
		}
		verbindung.beendeVerbindung();
		try {
			socket.close();
		} catch(IOException e) {
			System.err.println("Konnte den Verbindungs-Socket nicht mehr schliessen.");
		}
	}

	/**
	 * {@inheritDoc}
	 * Diese Methode kann von einer anderen Klasse ueberschrieben werden.<br />
	 * So wird man moeglichst einfach von neuen Nachrichten an den Client
	 * informiert. Natuerlich kann man auch direkt einen <code>Empfaenger</code>
	 * an diesem Client anmelden. Der Effekt ist derselbe.
	 * @see #empfaengerHinzufuegen(Empfaenger)
	 */
	@Override
	public void empfangeString(String string) {
		//To be overwritten
	}

	/**
	 * {@inheritDoc}
	 * Diese Methode kann von einer anderen Klasse ueberschrieben werden.<br />
	 * So wird man moeglichst einfach von neuen Nachrichten an den Client
	 * informiert. Natuerlich kann man auch direkt einen <code>Empfaenger</code>
	 * an diesem Client anmelden. Der Effekt ist derselbe.
	 * @see #empfaengerHinzufuegen(Empfaenger)
	 */
	@Override
	public void empfangeInt(int i) {
		//To be overwritten
	}

	/**
	 * {@inheritDoc}
	 * Diese Methode kann von einer anderen Klasse ueberschrieben werden.<br />
	 * So wird man moeglichst einfach von neuen Nachrichten an den Client
	 * informiert. Natuerlich kann man auch direkt einen <code>Empfaenger</code>
	 * an diesem Client anmelden. Der Effekt ist derselbe.
	 * @see #empfaengerHinzufuegen(Empfaenger)
	 */
	@Override
	public void empfangeByte(byte b) {
		//To be overwritten
	}

	/**
	 * {@inheritDoc}
	 * Diese Methode kann von einer anderen Klasse ueberschrieben werden.<br />
	 * So wird man moeglichst einfach von neuen Nachrichten an den Client
	 * informiert. Natuerlich kann man auch direkt einen <code>Empfaenger</code>
	 * an diesem Client anmelden. Der Effekt ist derselbe.
	 * @see #empfaengerHinzufuegen(Empfaenger)
	 */
	@Override
	public void empfangeDouble(double d) {
		//To be overwritten
	}

	/**
	 * {@inheritDoc}
	 * Diese Methode kann von einer anderen Klasse ueberschrieben werden.<br />
	 * So wird man moeglichst einfach von neuen Nachrichten an den Client
	 * informiert. Natuerlich kann man auch direkt einen <code>Empfaenger</code>
	 * an diesem Client anmelden. Der Effekt ist derselbe.
	 * @see #empfaengerHinzufuegen(Empfaenger)
	 */
	@Override
	public void empfangeChar(char c) {
		//To be overwritten
	}

	/**
	 * {@inheritDoc}
	 * Diese Methode kann von einer anderen Klasse ueberschrieben werden.<br />
	 * So wird man moeglichst einfach von neuen Nachrichten an den Client
	 * informiert. Natuerlich kann man auch direkt einen <code>Empfaenger</code>
	 * an diesem Client anmelden. Der Effekt ist derselbe.
	 * @see #empfaengerHinzufuegen(Empfaenger)
	 */
	@Override
	public void empfangeBoolean(boolean b) {
		//To be overwritten
	}

	/**
	 * {@inheritDoc}
	 * Diese Methode kann von einer anderen Klasse ueberschrieben werden.<br />
	 * So wird man moeglichst einfach von neuen Nachrichten an den Client
	 * informiert. Natuerlich kann man auch direkt einen <code>Empfaenger</code>
	 * an diesem Client anmelden. Der Effekt ist derselbe.
	 * @see #empfaengerHinzufuegen(Empfaenger)
	 */
	@Override
	public void verbindungBeendet() {
		//To be overwritten
	}
}
