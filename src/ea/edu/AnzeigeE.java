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

package ea.edu;

import ea.game.Manager;
import ea.game.Ticker;
import ea.graphic.Text;
import ea.input.KlickReagierbar;
import ea.input.Maus;
import ea.input.RechtsKlickReagierbar;
import ea.input.TastenReagierbar;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Random;
/**
 * Die Anzeige ermoeglicht Punktedarstellung im EDU-Konzept.<br />
 * Zusaetzlich realisiert sie das <b>Interfacefreie</b> Reagieren auf Tastendruck und Ticken. Dies
 * jedoch wird einem Schueler, der nach dem EDU-Konzept lernt so nie vorkommen. Fuer den Lerneffekt wird
 * die Funktionalitaet in den paketexternen Klassen auf spezielle Interfaces beschraenkt.
 * @author Andonie
 */
public class AnzeigeE
extends Manager
implements Ticker, TastenReagierbar, KlickReagierbar, RechtsKlickReagierbar {

    /**
     * Der Linke (Punkte-)Text.
     */
    private final Text links;

    /**
     * Der rechte (Punkte-)Text.
     */
    private final Text rechts;
    
    /**
     * Der Text für den Bindestrich
     */
    private final Text strich;

    /**
     * Die Liste aller TICKER-Aufgaben
     */
    private final ArrayList<Auftrag> aufgabenT;

    /**
     * Die Liste aller TASTEN-Aufgaben
     */
    private final ArrayList<TastenAuftrag> aufgaben;
    
    /**
     * Die Liste aller KLICK-Aufgaben
     */
    private final ArrayList<KlickAuftrag> aufgabenKlick;

    /**
     * Die Aktuelle Runde des Tickers
     */
    private int runde = 0;
    
    /**
     * Die Maus des Fensters.
     */
    private Maus maus;
    
    /**
     * Zufallsgenerator
     */
    private Random random = new Random();


    /**
     * Konstruktor. Erstellt die Texte fuer Links- und Rechtspunkte.
     * @param breite	Die gew�nschte Breite der Anzeige in Pixel.
     * @param hoehe		Die gew�nschtte H�he der Anzeige in Pixel.
     */
    public AnzeigeE(int breite, int hoehe) {
        links = new Text(0, 10, "0");
        rechts = new Text(0, 10, "0");
        aufgaben = new ArrayList<TastenAuftrag>();
        aufgabenT = new ArrayList<Auftrag>();
        aufgabenKlick = new ArrayList<KlickAuftrag>();
        FensterE.getFenster(breite, hoehe).wurzel.add(links, rechts, strich =  new Text(0, 10, "-"));
        FensterE.getFenster(breite, hoehe).tastenReagierbarAnmelden(this);
        super.anmelden(this, 1);
        punkteAnzeigen(false);
        punkteAlignen();
    }

    /**
     * Interne Align-Methode f�r harmonisches Aussehen der Punkte
     */
    private void punkteAlignen() {
		int lLinks = links.dimension().breite;
		int lRechts = rechts.dimension().breite;
		int lStrich = strich.dimension().breite;
		
		int groesser = lLinks>lRechts ? lLinks : lRechts;
		
		int breite = FensterE.getFenster().fensterGroesse().breite;
		
		strich.positionSetzen((breite-lStrich)/2, 10);
		links.positionSetzen(((breite-lStrich)/2)-groesser-5, 10);
		rechts.positionSetzen((breite+lStrich)/2+5, 10);
	}


	public int zufallszahlVonBis(int von, int bis) {
        if(von > bis) {
            System.err.println("Die Zufallszahl von ("+von+") war gr��er als die "
                    + "Zufallszahl bis (" + bis +").");
            return -1;
        }
        return random.nextInt(bis-von+1) + von;
    }
    
    /**
     * Setzt den Punktestand auf der linken Seite.
     * @param punkte    Der neue darzustellende Punktestand der linken Seite
     */
    public void punkteLinksSetzen(int punkte) {
        links.inhaltSetzen(""+punkte);
        punkteAlignen();
    }
    
    /**
     * Setzt, ob der Punktestand auf der linken Seite sichtbar sein soll oder 
     * nicht. <b>Nur wenn beide Texte links und rechts sichtbar sind, ist 
     * auch der Strich in der Mitte sichtbar.</b>
     * @param sichtbar Ob der Linke Text sichtbar sein soll.
     */
    public void punkteLinksSichtbarSetzen(boolean sichtbar) {
        links.sichtbarSetzen(sichtbar);
        strich.sichtbarSetzen(links.sichtbar() && rechts.sichtbar());
    }

    /**
     * Setzt den Punktestand auf der rechten Seite.
     * @param punkte    Der neue darzustellende Punktestand der rechten Seite
     */
    public void punkteRechtsSetzen(int punkte) {
        rechts.inhaltSetzen(""+punkte);
        punkteAlignen();
    }
    
    /**
     * Setzt, ob der Punktestand auf der rechten Seite sichtbar sein soll oder 
     * nicht. <b>Nur wenn beide Texte links und rechts sichtbar sind, ist 
     * auch der Strich in der Mitte sichtbar.</b>
     * @param sichtbar Ob der Linke Text sichtbar sein soll.
     */
    public void punkteRechtsSichtbarSetzen(boolean sichtbar) {
        rechts.sichtbarSetzen(sichtbar);
        strich.sichtbarSetzen(links.sichtbar() && rechts.sichtbar());
    }
    
    /**
     * Setzt, ob die Punkte angezeigt werden sollen.
     * @param visible Bei <code>true</code> ist die Punkteanzeige sichtbar, 
     * bei <code>false</code> ist sie unsichtbar.
     */
    public void punkteAnzeigen(boolean visible) {
        rechts.sichtbarSetzen(visible);
        links.sichtbarSetzen(visible);
        strich.sichtbarSetzen(visible);
    }


    // <editor-fold defaultstate="collapsed" desc="Reflection-Nutzung">
    /**
     * Meldet ein Objekt zum Ticken an. Intern laesst sich theoretisch ein Objekt <b>JEDER</b> Klasse
     * anmelden!<br />
     * Deshalb <i>sollten nur Objekte angemeldet werden, die Instanzen des EDU-<code>TICKER</code>-Interfaces sind!!</i>
     * @param o             Das anzumeldende Objekt, dessen Tickermethode aufgerufen werden soll.<br />
     * Es <b>MUSS</b> eine Methode <code>tick()</code> haben.
     * @param   intervall   Das Intervall in Millisekunden, in dem das anzumeldende Objekt aufgerufen.
     * @see ea.game.Ticker
     * @see tickerAbmelden(Object)
     */
    public void tickerAnmelden(Object o, int intervall) {
        Class<?> klasse = o.getClass();
        Method[] methoden = klasse.getMethods();
        for (int i = 0; i < methoden.length; i++) {
            if (methoden[i].getName().equals("tick")) {
                aufgabenT.add(new Auftrag(o, methoden[i], intervall));
                return;
            }
        }
    }
    
    

    /**
     * Meldet einen "Ticker" ab.
     * @param o Das Angemeldete "Ticker"-Objekt, das nun nicht mehr aufgerufen werden soll.
     * @see tickerAnmelden(Object, int)
     */
    public void tickerAbmelden(Object o) {
        Auftrag re = null;
        for(Auftrag a : aufgabenT) {
            if(a.client().equals(o)) {
                re = a;
            }
        }
        if(re != null) {
            aufgabenT.remove(re);
        }
    }

    /**
     * Meldet ein Objekt an, das ab sofort auf Tastendruck reagieren wird.<br />
     * Intern laesst sich theoretisch ein Objekt <b>JEDER</b> Klasse
     * anmelden!<br />
     * Deshalb <i>sollten nur Objekte angemeldet werden, die Instanzen des EDU-<code>TASTENREAGIERBARANMELDEN</code>-Interfaces sind!!</i>
     * @param o    Das anzumeldende Objekt. Dieses wird ab sofort ueber jeden Tastendruck informiert.
     * @see ea.input.TastenReagierbar
     */
    public void tastenReagierbarAnmelden(Object o) {
        Class<?> klasse = o.getClass();
        Method[] methoden = klasse.getMethods();
        for (int i = 0; i < methoden.length; i++) {
            if (methoden[i].getName().equals("tasteReagieren")) {
                aufgaben.add(new TastenAuftrag(o, methoden[i]));
                return;
            }
        }
    }
    
    /**
     * Meldet ein Objekt an, das ab sofort auf Mausklicks reagieren wird.<br />
     * Intern laesst sich theoretisch ein Objekt <b>JEDER</b> Klasse
     * anmelden!<br />
     * Deshalb <i>sollten nur Objekte angemeldet werden, die Instanzen eines interfaces EDU-<code>KLICKREAGIERBAR</code>-Interfaces sind!!</i><br />
     * <br />
     * <br />
     * Example:<br />
     * <b />
     * <code>KLICKREAGIERBAR { <br />
     * //Eine Methode diesen Namens MUSS existieren!!<br />
     * public abstract void klickReagieren(int x, int y);<br />
     * }</code>
     * 
     * @param client    Das anzumeldende Objekt. Dieses wird ab sofort ueber jeden Mausklick informiert.
     * @see ea.input.KlickReagierbar
     * @see ea.input.RechtsKlickReagierbar
     */
    public void klickReagierbarAnmelden(Object o, boolean linksklick) {
        if(maus == null) {
            //Erstmal Maus erstellen
            maus = new Maus(1);
            FensterE.getFenster().mausAnmelden(maus);
            maus.klickReagierbarAnmelden(this);
        }
        Class<?> klasse = o.getClass();
        Method[] methoden = klasse.getMethods();
        for (int i = 0; i < methoden.length; i++) {
            if (methoden[i].getName().equals("klickReagieren")) {
                aufgabenKlick.add(new KlickAuftrag(o, methoden[i], linksklick));
                return;
            }
        }
    }
    
    /**
     * Reagiert auf einen Linksklick
     * @param x X-Koordinate des Klicks
     * @param y Y-Koordinate des Klicks
     */
    public void klickReagieren(int x, int y) {
        klickSub(x,y,true);
    }

    /**
     * Reagiert auf einen Rechtsklick
     * @param x X-Koordinate des Klicks
     * @param y Y-Koordinate des Klicks
     */
    public void rechtsKlickReagieren(int x, int y) {
        klickSub(x,y,false);
    }
    
    /**
     * Sublogik fuer Mausklicks.
     * @param x X-Koordinate des Klicks
     * @param y Y-Koordinate des Klicks
     * @param links Ob der Klick ein Linksklick war oder nicht
     */
    private void klickSub(int x, int y, boolean links) {
        for(KlickAuftrag a : aufgabenKlick) {
            if(a.linksklick==links) {
                a.ausfuehren(x, y);
            }
        }
    }

    /**
     * Methode zum Weiterleiten von Tastendrucks an die angemeldeten
     * @param code Der Tastaturcode des Tastendrucks
     * @see ea.input.TastenReagierbar
     */
    @Override
    public void reagieren(int code) {
        for (TastenAuftrag t : aufgaben) {
            t.ausfuehren(code);
        }
    }

    /**
     * In der TICK-Methode wird die Weitergabe des TICK-Befehls geregelt.
     */
    @Override
    public void tick() {
        try {
            for (Auftrag a : aufgabenT) {
                if (runde % a.intervall() == 0) {
                    a.ausfuehren();
                }
            }
            runde++;
        } catch(java.util.ConcurrentModificationException e) { }
    }

    /**
     * Ein Auftrag regelt je einen Fake-Ticker.
     */
    private final class Auftrag {

        /**
         * Das Intervall
         */
        private final int intervall;
        /**
         * Der Client, an dem der Tick aufgerufen wird
         */
        private final Object client;
        /**
         * Die aufzurufende TICK-MEthode
         */
        private final Method methode;

        public Auftrag(Object client, Method tick, int intervall) {
            this.intervall = intervall;
            this.client = client;
            methode = tick;
        }

        /**
         * Fuehrt einen Tick aus.
         */
        public final void ausfuehren() {
            try {
                methode.invoke(client, new Object[0]);
            } catch (InvocationTargetException ex) {
                //
            } catch (java.lang.IllegalAccessException e) {
                //
            }
        }

        /**
         * @return  Das Intervall des gelagerten Objektes
         */
        public int intervall() {
            return intervall;
        }

        /**
         * @return  Das Objekt, das als "Client"-Ticker immer wieder aufgerufen wird.
         */
        public Object client() {
            return client;
        }
    }

    /**
     * Ein TastenAuftrag regelt den Aufruf eines TastenReaktions-Interface.
     */
    private final class TastenAuftrag {

        /**
         * Die aufzurufende Methode
         */
        private final Method methode;
        /**
         * Das Objekt, an dem diese Methode ausgefuehrt werden soll!
         */
        private final Object client;

        /**
         * Erstellt einen Tastenauftrag
         * @param client    Das Objekt, an dem der Auftrag ausgefuehrt werden soll.
         * @param m         Die auszufuehrende Methode.
         */
        public TastenAuftrag(Object client, Method m) {
            this.client = client;
            methode = m;
        }

        /**
         * Fuehrt die Methode einmalig aus.
         * @param code  Der Tastaturcode, der mitgegeben wird.
         */
        public void ausfuehren(int code) {
            try {
                methode.invoke(client, code);
            } catch (InvocationTargetException ex) {
                //
            } catch (java.lang.IllegalAccessException e) {
                //
            }
        }
    }
    
    /**
     * Auftrag fuer einen Klick-Listener
     */
    private final class KlickAuftrag {
        private final Method methode;
        
        private final Object client;
        
        private final boolean linksklick;
        
        private KlickAuftrag(Object c, Method m, boolean linksklick) {
            methode = m;
            client = c;
            this.linksklick = linksklick;
        }
        
        /**
         * Fuehrt die Methode am Client aus.
         * @param x Die zu uebergebene X-Koordinate 
         * des Klicks.
         * @param y Die zu uebergebene Y-Koordinate 
         * des Klicks.
         */
        private void ausfuehren(int x, int y) {
            try {
                methode.invoke(client, new Object[] {x, y});
            } catch (InvocationTargetException ex) {
                //
            } catch (java.lang.IllegalAccessException e) {
                //
            }
        }
    }
    // </editor-fold>
}

