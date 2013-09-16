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

package ea.graphic;

import ea.game.Manager;
import ea.graphic.geo.Punkt;
import ea.graphic.geo.Raum;

/**
 * Ein Linienanimierer laesst ein Raum-Objekt zwischen verschiedenen Punkten zusammenlaufen.
 * sich fest zwischen 2 Punkten gleichmaessig hin- und herbewegen.
 * 
 * @author (Ihr Name) 
 * @version (eine Versionsnummer oder ein Datum)
 */
public class StreckenAnimierer 
extends Animierer
{
    /**
     * Das Array aller Punkte, die von diesem Animierer nacheinander abgefahren werden.
     */
    private final Punkt[] punkte;
    
    /**
     * Der Vektor, der die Bewegung beschreibt.
     */
    private Vektor vektor;
    
    /**
     * Der Vektor, der die Modulo-Bewegung fuer Bewegung festhaelt.<br />
     * Bei einer nichtrestlosen Teilung der Bewegung wird der Rest auf diesen Vektor uebertragen.
     */
    private Vektor modulo;
    
    /**
     * Der Count, der Angibt, an welchen Punkt sich das zu animierende Objekt gerade naehern soll.
     */
    private int punkteCount;
    
    /**
     * Gibt an, ob gerade vorwaerts animiert wird.
     */
    private boolean vorwaerts = true;
    
    /**
     * Gibt an, ob prinzipiell im Kreis, oder strickt vorwaerts und anschliessend rueckwaerts animiert wird.<br />
     * Ist nur von Belang, wenn die Animation geloopt sein soll.
     */
    private boolean circuit = true;
    
    /**
     * Erstellt ein Objekt dieser Klasse zum Animieren.
     * @param   ziel    Das zu animierende Objekt
     * @param   loop    Ob die Animation dauerhaft wiederholt (geloopt) werden soll.
     * @param   circuit Gibt an, ob der letzte und der erste Punkt bei Wiederholung ebenfalls miteinander verbunden animiert werden sollen (So dass immer ein geschlossener Kreislauf entsteht)
     * @param   m   Der Manager, an dem animiert werden soll.
     * @param   geschwindigkeit Die Geschwindigkeit, mit der die Bewegung stattfinden soll.<br />
     * Sie gibt in Millisekunden an, wie lang die Bewegung zwischen 2 angegebenen Punkten dauern soll. 
     * @param   listener    Der AnimationsEndeReagierbar-Listener, der am Ende der Animation aufgerufen wird.
     * @param   zielPunkte  Nacheinander alle Punkte, die die Animation ueberlaufen soll.
     */
    public StreckenAnimierer(Raum ziel, boolean loop, boolean circuit, Manager m, int geschwindigkeit, AnimationsEndeReagierbar listener, Punkt... zielPunkte) {
        super(ziel, (geschwindigkeit/schritte), loop, m, listener);
        this.circuit = circuit;
        punkte = new Punkt[zielPunkte.length + 1];
        this.punkte[0] = ziel.zentrum();
        for(int i = 1; i < punkte.length; i++) {
            punkte[i] = zielPunkte[i - 1];
        }
        punkteCount = 1;
        Punkt p2 = punkte[1];
        vektor = new Vektor((p2.x - punkte[0].x)/schritte, (p2.y-punkte[0].y)/schritte);
        modulo = new Vektor((p2.x - punkte[0].x)%schritte, (p2.y-punkte[0].y)%schritte);
    }
    
    /**
     * Leicht vereinfachter Konstruktor der Klasse
     * @param   ziel    Das zu animierende Objekt
     * @param   loop    Ob die Animation dauerhaft wiederholt (geloopt) werden soll, <b> sowie</b>, ob die animation im Kreislauf stattfinden soll
     * @param   m   Der Manager, an dem animiert werden soll.
     * @param   geschwindigkeit Die Geschwindigkeit, mit der die Bewegung stattfinden soll.<br />
     * Sie gibt in Millisekunden an, wie lang die Bewegung zwischen 2 angegebenen Punkten dauern soll.  
     * @param   listener    Der AnimationsEndeReagierbar-Listener, der am Ende der Animation aufgerufen wird.
     * @param   zielPunkte  Nacheinander alle Punkte, die die Animation ueberlaufen soll.
     */
    public StreckenAnimierer(Raum ziel, boolean loop, Manager m, int geschwindigkeit, AnimationsEndeReagierbar listener, Punkt... zielPunkte) {
        this(ziel, loop, loop, m, geschwindigkeit, listener, zielPunkte);
    }
    
    /**
     * Vereinfachter Konstruktor der Klasse.<br />
     * Hierbei wird <b>automatisch</b> in einer Dauerschleife und als Kreislauf animiert.
     * @param   ziel    Das zu animierende Objekt
     * @param   m   Der Manager, an dem animiert werden soll.
     * @param   geschwindigkeit Die Dauer der Bewegung zwischen 2 "Etappen"-Punkten in Millisekunden.<br />
     * Sie gibt in Millisekunden an, wie lang die Bewegung zwischen 2 angegebenen Punkten dauern soll.  
     * @param   listener    Der AnimationsEndeReagierbar-Listener, der am Ende der Animation aufgerufen wird.
     * @param   zielPunkte  Nacheinander alle Punkte, die die Animation ueberlaufen soll.
     */
    public StreckenAnimierer(Raum ziel, Manager m, int geschwindigkeit, AnimationsEndeReagierbar listener, Punkt... zielPunkte) {
        this(ziel, true, true, m, geschwindigkeit, listener, zielPunkte);
    }
    
    public void animationsSchritt() {
        ziel.bewegen(vektor);
        if(count == schritte) {
            ziel.bewegen(modulo);
            Punkt p1 = punkte[punkteCount];
            Punkt p2 = null;
            if(punkteCount == (punkte.length - 1) && vorwaerts) {
                if(loop) {
                    if(circuit) {
                        punkteCount = 0;
                        p2 = punkte[punkteCount];
                    } else {
                        vorwaerts = !vorwaerts;
                    }
                } else {
                    beenden();
                    return;
                }
            } else if(punkteCount == 0 && !vorwaerts) {
                if(loop) {
                    if(!circuit) {
                        vorwaerts = !vorwaerts;
                    } else {
                        //Passiert nie.
                    }
                } else {
                    beenden();
                    return;
                }
            }
            if(p2 == null) {
                if(!vorwaerts) {
                    punkteCount--;
                } else {
                    punkteCount++;
                }
                p2 = punkte[punkteCount];
            }
            vektor = new Vektor((p2.x - p1.x)/schritte, (p2.y-p1.y)/schritte);
            modulo = new Vektor((p2.x - p1.x)%schritte, (p2.y-p1.y)%schritte);
            count = 0;
        }
    }
}
