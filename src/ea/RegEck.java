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

import ea.graphic.geo.Dreieck;
import ea.graphic.geo.Geometrie;
import ea.graphic.geo.Punkt;

/**
 * Basisklasse fuer ein regelmaessiges n-Eck.<br />
 * Tatsaechlich ist aufgrund von moeglicherweise Auftretenden Rundungsfehlern dies ein n+1 - Eck.<br />
 * Dies faellt jedoch nicht auf und ist nur da um eventuell auftretende Abrundungsfehler wieder auszuduennen.
 * 
 * @author (Ihr Name) 
 * @version (eine Versionsnummer oder ein Datum)
 */
public class RegEck
extends Geometrie
{
    /**
     * Die Anzahl an Ecken.<br />
     * Es kann keine Form mit weniger als 3 Ecken geben!
     */
    protected final int eckenzahl;
    
    /**
     * Der Radius des Umkreises des n-Ecks
     */
    protected int radius;
    
    /**
     * Konstruktor fuer Objekte der Klasse N-Eck
     * @param   x   Die X-Koordinate der Linken oberen Ecke des das n-Eck umschreibenden Rechtecks, <b>nicht die des Mittelpunktes</b>
     * @param   y   Die Y-Koordinate der Linken oberen Ecke des das n-Eck umschreibenden Rechtecks, <b>nicht die des Mittelpunktes</b>
     * @param   ecken   Die Anzahl der Ecken des Ecks
     * @param   durchmesser     Der Durchmesser des Kreises, der das n-Eck umschreibt
     */
    public RegEck(int x, int y, int ecken, int durchmesser) {
        super(x, y);
        eckenzahl = ecken;
        this.radius = durchmesser/2;
        aktualisierenFirst();
    }
    
    /**
     * Setzt einen neuen Durchmesser fuer das regelmaessige n-Eck.
     * @param   durchmesser Der neue Durchmesser
     */
    public void durchmesserSetzen(int durchmesser) {
        this.radius = durchmesser/2;
        aktualisieren();
    }
    
    /**
     * Setzt einen neuen Radius fuer das regelmaessige n-Eck.
     * @param radius    Der neue Radius.
     */
    public void radiusSetzen(int radius) {
        this.radius = radius;
        aktualisieren();
    }

    /**
     * Rundet einen Doublewert. (Mit Gauss'scher Treppenfunktion)
     * @return  Der auf Integer gerundetete Wert der Zahl.
     */
    public static int runden(double d) {
        return (int)Math.floor(d + 0.5);
    }
    
    /**
     * In dieser Methode werden saemtliche Dreiecke neu berechnet und die Referenz bei Aufruf in der Superklasse hierauf gesetzt.<br />
     * Hierbei wird ein Dreieck mehr zurueckgegeben als Ecken eingegeben wurden, um das Eck auch schliessen zu koennen. Dies ist aufgrund des rundens innerhalb des Algorythmusses nicht garantiert.
     */
    public Dreieck[] neuBerechnen() {
        Dreieck[] ret = new Dreieck[eckenzahl+1];
        double winkelSchritt = (360/eckenzahl)*(Math.PI/180);
        double winkel = 0;
        int x = super.x+(radius);
        int y = super.y+(radius);
        final Punkt zentrum = new Punkt(x+(radius/2), y+(radius/2));
        Punkt letzter = new Punkt(runden((Math.sin(winkel+(Math.PI/2))*radius))+x, runden((Math.sin(winkel)*radius))+y);
        final Punkt erster = letzter;
        for(int i = 0; i < ret.length-1; i++) {
            winkel = winkel+winkelSchritt;
            int koordX = runden((Math.sin(winkel+(Math.PI/2))*radius))+x;
            int koordY = runden((Math.sin(winkel)*radius))+y;
            Punkt neuer = new Punkt(koordX, koordY);
            ret[i] = new Dreieck( zentrum, letzter, neuer);
            letzter = neuer;
        }
        ret[ret.length-1] = new Dreieck(zentrum, letzter, erster);
        return ret;
    }
}
