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

package ea.input;

/**
 * MausReagierbar ist ein Interface, das die Reaktionsmoeglichkeit auf einen Mausklick auf ein bestimmtes Objekt darstellt.
 * 
 * @author (Ihr Name) 
 * @version (eine Versionsnummer oder ein Datum)
 */

public interface MausReagierbar
{
    /**
     * Die Reaktion auf ein bestimmtes angeklicktes Element.
     * @param   code    Der der Maus bei der Anmeldung vorgegebene Code, der fuer ein bestimmtes angeklicktes Objekt steht.<br />
     * Demnach muss also bei der Maus ein Reagierbar mitsamt dem zu Ueberwachenden Raum-Objekt und dem Code, der dann in dieser Methode als Argument mitgegeben wird
     * angemeldet werden.
     */
    public abstract void mausReagieren(int code);
}
