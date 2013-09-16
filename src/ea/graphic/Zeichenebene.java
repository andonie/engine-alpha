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

import ea.graphic.geo.Knoten;

/**
 * Die Zeichenebene ist die Sammlung aller zu malenden Objekte.<br />
 * Aufgrund ihrer Einfachheit hat sie mehr symbolische Bedeutung.
 * 
 * @author (Ihr Name) 
 * @version (eine Versionsnummer oder ein Datum)
 */
public class Zeichenebene
{
    /**
     * Der absolute Basisknoten.<br />
     * Hieran
     */
    private Knoten basis = new Knoten();

    /**
     * Konstruktor f�r Objekte der Klasse Zeichenebene
     */
    public Zeichenebene()
    {
        //
    }
    
    /**
     * @return  Der Basisknoten
     */
    public Knoten basis() {
        return basis;
    }
}
