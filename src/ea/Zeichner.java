/*
 * Engine Alpha ist eine anfaengerorientierte 2D-Gaming Engine.
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

package ea;

import ea.graphic.AnimationsManager;
import ea.graphic.SimpleGraphic;
import ea.graphic.Vektor;
import ea.graphic.geo.BoundingRechteck;
import ea.graphic.geo.Raum;
import ea.physics.Physik;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferStrategy;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Dies ist das Panel, in dem die einzelnen Dinge gezeichnet werden
 * 
 * @author (Ihr Name)
 * @version (eine Versionsnummer oder ein Datum)
 */
public class Zeichner extends Canvas implements Runnable {

	/**
	 * Die Kamera.<br />
	 * Letzendlich wird das gezeichnet, was sich in ihr befindet
	 */
	private Kamera cam;

	/**
	 * Das BoundingRechteck, dass das Panel in seiner Groesse beschreibt.
	 */
	private BoundingRechteck groesse;

	/**
	 * Der Knoten, der die statischen Objekte beinhaltet.
	 */
	private Knoten statNode = new Knoten();

	/**
	 * Der Relative Hintergrund, ist immer das Hinterste.
	 */
	private Raum hintergrund;

	/**
	 * Der Absolute Vordergrund. Er liegt immer im Zentrum<br />
	 * Reserviert fuer die Absolute Maus.
	 */
	private Raum vordergrund;

	/**
	 * Gibt an, ob der Thread noch arbeiten soll.
	 */
	private boolean work = true;

	/**
	 * Die Liste der einfachen Geometrischen Koerper, die gezeichnet werden
	 * sollen.
	 * 
	 * @see ea.graphic.SimpleGraphic
	 */
	private final CopyOnWriteArrayList<SimpleGraphic> simples = new CopyOnWriteArrayList<SimpleGraphic>();

	/**
	 * Konstruktor fuer Objekte der Klasse Zeichner
	 * 
	 * @param x
	 *            Die Groesse des Einflussbereichs des Panels in Richtung X.
	 * @param y
	 *            Die Groesse des Einflussbereichs des Panels in Richtung Y.
	 * @param c
	 *            Die Kamera, deren Sicht grafisch dargestellt werden soll.
	 */
	public Zeichner(int x, int y, Kamera c) {
		this.setSize(x, y);
		this.setPreferredSize(getSize());
		this.setFocusable(true);

		groesse = new BoundingRechteck(0, 0, x, y);
		cam = c;
	}

	public void init() {
		new Thread(this, "Zeichenthread").start();
	}

	/**
	 * run-Methode. Implementiert aus <code>Runnable</code>.<br />
	 * Hierin findet in einer dauerschleife die Zeichenroutine statt.
	 */
	public void run() {
		createBufferStrategy(2);
		BufferStrategy bs = getBufferStrategy();
		Graphics2D g = (Graphics2D) bs.getDrawGraphics();
		
		if(g instanceof Graphics2D) { // Kantenglaettung
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
		}

		while (work) {
			render(g);
			
			if(this.isDisplayable())
				bs.show();
			else break;
			
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				//
			}
		}
	}

	/**
	 * Toetet den Zeichenprozess und entfernt alle Elemente von der Wurzel und
	 * neutralisiert die Phyisk.
	 */
	public void kill() {
		work = false;
		Physik.neutralize();
		AnimationsManager.neutralize();
	}

	/**
	 * @return Die Kamera, die dieser Zeichner aufruft
	 */
	public Kamera cam() {
		return cam;
	}

	/**
	 * @return Der statische Basisknoten
	 */
	public Knoten statNode() {
		return statNode;
	}

	/**
	 * Meldet einen Vordergrund an.
	 * 
	 * @param vordergrund
	 *            Der neue Vordergrund
	 */
	void anmelden(Raum vordergrund) {
		this.vordergrund = vordergrund;
	}

	/**
	 * Meldet den zu zeichnenden Hintergrund an.
	 * 
	 * @param hintergrund
	 *            Der neue Hintergrund
	 */
	public void hintergrundAnmelden(Raum hintergrund) {
		this.hintergrund = hintergrund;
	}

	/**
	 * Loescht den absoluten Vordergrund
	 */
	void vordergrundLoeschen() {
		vordergrund = null;
	}

	/**
	 * @return Ein BoundingRechteck, dass die BReite und Hoehe des Fensters hat.
	 */
	public BoundingRechteck masse() {
		return groesse;
	}

	public void addSimple(SimpleGraphic g) {
		simples.add(g);
	}

	public void removeSimple(SimpleGraphic g) {
		simples.remove(g);
	}

	/**
	 * Die render()-Methode, sie fuehrt die gesamte Zeichenroutine aus.
	 * 
	 * @param g
	 *            Das zum Zeichnen uebergebene Graphics-Objekt
	 */
	public void render(Graphics g) {
		// Absoluter Hintergrund
		g.setColor(Color.black);
		g.fillRect(0, 0, groesse.breite, groesse.hoehe);
		// Relativer Hintergrund
		if (hintergrund != null) {
			hintergrund.zeichnenBasic(g, groesse.verschobeneInstanz(new Vektor(
					cam.getX() / 5, cam.getY() / 10)));
		}
		// Die Objekte
		cam.zeichne(g);
		// Die simplen Grafikobjekte (nicht in Raum)
		BoundingRechteck camBounds = cam.position();
		for (SimpleGraphic gr : simples) {
			gr.paint(g, camBounds.x, camBounds.y);
		}
		// Die statischen Objekte
		statNode.zeichnen(g, groesse);
		// Die Maus
		if (vordergrund != null) {
			vordergrund.zeichnen(g, groesse);
		}
	}
}
