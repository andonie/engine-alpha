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

package ea.graphic.geo;

import ea.graphic.AnimationsManager;
import ea.graphic.StehReagierbar;
import ea.graphic.Vektor;
import ea.graphic.lights.LeuchtMacher;
import ea.graphic.lights.Leuchtend;
import ea.graphic.windows.Fenster;
import ea.io.DateiManager;
import ea.network.NullClient;
import ea.physics.FallReagierbar;
import ea.physics.Gravitator;
import ea.physics.Passivator;
import ea.physics.PhysikClient;

import java.awt.*;
import java.util.Locale;
/**
 * Raum bezeichnet alles, was sich auf der Zeichenebene befindet.<br />
 * Dies ist die absolute Superklasse aller grafischen Objekte. Umgekehrt kann somit jedes
 * grafische Objekt die folgenden Methoden nutzen.
 * 
 * @author Michael Andonie
 * @version (eine Versionsnummer oder ein Datum)
 */
public abstract class Raum
implements java.io.Serializable
{
    /**Die Serialisierungs-Konstante dieser Klasse. In keiner Weise fuer die Programmierung mit der Engine bedeutsam.*/
    private static final long serialVersionUID = 98L;

    /**
     * Der Leuchtmacher fuer alle Raum-Objekte
     */
    private static final LeuchtMacher macher = new LeuchtMacher();
    
    /**
     * Der Animations-Manager, ueber den alle Animationen laufen.<br />
     * Wird zum Loeschen aller Referenzen auf dieses Objekt verwendet.
     */
    private static final AnimationsManager animationsManager = AnimationsManager.getAnimationsManager();

    /**
     * Ob die Kollisionstests Roh oder fein ablaufen sollen.
     */
    protected static boolean roh = false;

    /**
     * Ein einfacher Farbzyklus, der fuer die Leucht-Animationen genommen wird
     */
    public static final Color[] farbzyklus = {
        Color.white,
        Color.blue,
        Color.red,
        Color.yellow,
        Color.magenta,
        Color.cyan,
        Color.green,
        Color.orange,
    };
    
    /**
     * Gibt an, ob das Objekt zur Zeit ueberhaupt sichtbar sein soll.<br />
     * Ist dies nicht der Fall, so wird die Zeichenroutine direkt uebergangen.
     */
    private boolean sichtbar = true;

    /**
     * Gibt an, ob dieses Objekt mit Verzug ungleich von 0 gezeichnet wuerde. In
     * diesem Fall wird es als statisch betrachtet. Ob dies tatsaechlich der Fall
     * ist, ist irrelevant.
     */
    private boolean statisch = false;

    /**
     * Der Physik-Client, der die Physik dieses Raum-Objekts regelt.
     */
    private PhysikClient phClient = new NullClient(this);
    
    /**
     * Der eine und einziege Konstruktor fuer Objekte der Klasse Raum.<br />
     * Hier passiert nichts fuer das Programmieren einer Raum-Klasse direkt relevantes, naemlich GAR NICHTS
     */
    public Raum() {
        //
    }

    /**
     * Setzt, ob saemtliche Kollisionstests in der Engine Alpha grob oder fein sein sollen.
     * @param heavy Ist dieser Wert <code>true</code>, werden intern Kollisionstests genauer, aber rechenintensiver. Ist er
     * <code>false</code>, werden diese Kollisionstests schneller, aber ungenauer.
     * @see ea.game.Game.rechenintensiveArbeiteSetzen(boolean)
     */
    public static void heavyComputingSetzen(boolean heavy) {
        roh = heavy;
    }

    /**
     * Macht dieses <code>Raum</code>-Objekt fuer die Physik zu einem <i>Neutralen</i> Objekt, also einem 
     * Objekt das per se nicht an der Physik teilnimmt.
     */
    public void neutralMachen() {
        phClient.aufloesen();
        phClient = new NullClient(this);
    }

    /**
     * Macht dieses Objekt zu einem Passiv-Objekt. <br />
     * Ab dem Aufruf dieser Methode verhaelt es sich als Boden-/Wand- bzw. Deckenelement
     * und haelt Aktiv-Objekte auf.
     */
    public void passivMachen() {
        phClient.aufloesen();
        phClient = new Passivator(this);
    }

    /**
     * Macht dieses Objekt zu einem Aktiv-Objekt.<br />
     * Ab dem Aufruf dieser Methode laesst es sich von Passiv-Objekten aufhalten und wird -
     * solange dies nicht ueber den Methodenaufruf <code>schwerkraftAktivSetzen(false)</code>
     * deaktiviert wird - von einer kuenstlichen Schwerkraft angezogen.
     * @see schwerkraftAktivSetzen(boolean)
     */
    public void aktivMachen() {
        phClient.aufloesen();
        phClient = new Gravitator(this);
    }

    /**
     * Laesst das <code>Raum</code>-Objekt einen Sprung von variabler Kraft machen. Dies funktioniert
     * jedoch nur dann, wenn das Objekt auch ein <i>Aktiv-Objekt</i> ist. Ansonsten ist wird hier
     * eine Fehlermeldung ausgegeben.
     * @param kraft Die Kraft dieses Sprunges. Je hoeher dieser Wert, desto hoeher der Sprung.
     * @return  <code>true</code>, wenn das <code>Raum</code>-Objekt erfolgreich springen konnte. <code>false</code>,
     * wenn das <code>Raum</code>-Objekt <b>nicht</b> springen konnte.<br />
     * Zweiteres ist automatisch immer dann der Fall, wenn<br />
     * - das <code>Raum</code>-Objekt <b>kein Aktiv-Objekt mit aktivierter Schwerkraft (Standard)</b> ist oder <br />
     * - das <code>Raum</code>-Objekt als Aktiv-Objekt <b>nicht auf einem Passiv-Objekt</b> steht.
     */
    public boolean sprung(int kraft) {
        return phClient.sprung(kraft);
    }

    /**
     * Setzt, ob dieses <code>Raum</code>-Objekt von Schwerkraft beeinflusst wird. Macht nur dann Sinn,
     * wenn das Objekt, an dem diese Methode ausgefuehrt wird, ein Aktiv-Objekt ist.
     * @param aktiv <code>true</code>, wenn Schwerkraft aktiv sein soll, sonst <code>false</code>.
     * @see aktivMachen()
     */
    public void schwerkraftAktivSetzen(boolean aktiv) {
        phClient.schwerkraftAktivSetzen(aktiv);
    }
    
    /**
     * Setzt die Schwerkraft fuer dieses spezielle Objekt.<br />
     * <b>Achtung:</b>
     * Standardwert: 4<br />
     * Groesserer Wert = langsamer Fallen<br />
     * Kleinerer Wert = schneller Fallen <br />
     * Negativer Wert : Moege Gott uns allen gnaedig sein...
     * @param schwerkraft Der Wert fuer die Schwerkraft der Physik.<br />
     * <b>Wichtig:</b> Dies repraesentiert <i>keinen</i> Wert fuer die (Erd-)
     * Beschleunigungszahl "g" aus der Physik. Schon allein deshalb, weil die 
     * Zahl umgekehrt wirkt (s. oben).
     * @see Raum#aktivMachen()
     */
    public void schwerkraftSetzen(int schwerkraft) {
        this.phClient.schwerkraftSetzen(schwerkraft);
    }

    /**
     * Meldet einen <code>FallReagierbar</code>-Listener an.<br />
     * Dieser wird ab sofort immer dann informiert, wenn dieses <code>Raum</code>-Objekt unter eine bestimmte
     * Hoehe faellt. Diese wird als <b>kritische Tiefe</b> bezeichnet. Der Listener wird ab diesem Zeitpunkt
     * <i>dauerhaft aufgerufen, solange das Objekt unterhalb dieser Toleranzgrenze ist</i>. Deshalb sollte in der
     * implementierten Reaktionsmethode des <code>FallReagierbar</code>-Interfaces die Hoehe so neu gesetzt werden, dass
     * das <code>Raum</code>-Objekt nicht mehr unterhalb der <b>kritischen Tiefe</b> ist.<br /><br />
     * <b>ACHTUNG!</b><br />
     * Jedes <code>Raum</code>-Objekt hat <b>HOECHSTENS</b> einen <code>FallReagierbar</code>-Listener. Das bedeutet, dass
     * es <b>nicht moeglich ist, dass mehrere <code>FallReagierbar</code>-Listener ueber ein Objekt informiert werden.<br /><br />
     * Die <b>kritische Tiefe</b> jedoch laesst sich problemlos immer wieder neu Setzen, ueber die Methode
     * <code>kritischeTiefeSetzen(int tiefe)</code>.<br /><br />
     * Diese Methode mach natuerlich nur Sinn, wenn sie an einem <i>Aktiv-Objekt</i> ausgefuehrt wird. Andernfalls gibt es eine Fehlermeldung!
     * @param f Das anzumeldende <code>FallReagierbar
     * @param kritischeTiefe    Die Tiefe ab der der Listener <i>dauerhaft</i> durch den Aufruf seiner Reaktionsmethode
     * informiert wird, solange das <code>Raum</code>-Objekt hierunter ist.
     * @see ea.physics.FallReagierbar
     * @see kritischeTiefeSetzen(int)
     */
    public void fallReagierbarAnmelden(FallReagierbar f, int kritischeTiefe) {
        phClient.fallReagierbarAnmelden(f, kritischeTiefe);
    }

    /**
     * Setzt die <b>kritische Tiefe</b> neu. Ab dieser Tiefe wird der <code>FallReagierbar</code>-Listener dieses <code>Raum</code>-Objektes
     * aufgerufen - dauerhaft so lange, bis das <code>Raum</code>-Objekt <b>nicht mehr unterhalb dieser Tiefe ist</b>.
     * @param tiefe Die neue kritische Tiefe. Die Tiefe ab der der Listener <i>dauerhaft</i> durch den Aufruf seiner Reaktionsmethode
     * informiert wird, solange das <code>Raum</code>-Objekt hierunter ist.
     * @see ea.physics.FallReagierbar
     * @see fallReagierbarAnmelden(FallReagierbar, int)
     */
    public void kritischeTiefeSetzen(int tiefe) {
        phClient.kritischeTiefeSetzen(tiefe);
    }

    /**
     * Diese Methode meldet einen <code>StehReagierbar</code>-Listener neu an.<br />
     * Dieser wird nach der Anmeldung immer dann <i>einmalig</i> durch den Aufruf seiner <code>stehReagieren()</code>-Methode informiert,
     * wenn dieses <code>Raum</code>-Objekt nach dem Fall/Sprung wieder auf einem Passiv-Objekt zu stehen kommt.<br /><br />
     * <b>ACHTUNG!</b><br />
     * Ein <code>Raum</code>-Objekt kann <b>hoechstens einen <code>StehReagierbar</code>-Listener besitzen</b>!<br /><br />
     * Diese Methode mach natuerlich nur Sinn, wenn sie an einem <i>Aktiv-Objekt</i> ausgefuehrt wird. Andernfalls gibt es eine Fehlermeldung!
     * @param s Der <code>StehReagierbar</code>-Listener, der ab sofort bei jedem neuen zum Stehen kommen dieses <code>Raum</code>-Objekts
     * informiert wird.
     * @see ea.graphic.StehReagierbar
     */
    public void stehReagierbarAnmelden(StehReagierbar s) {
        phClient.stehReagierbarAnmelden(s);
    }

    /**
     * Prueft, ob dieses Objekt als <i>Aktiv-Objekt</i> steht.<br />
     * Diese Methode steht nicht in direktem Zusammenhang mit dem Interface <code>StehReagierbar</code>, denn durch diese Methode  laesst
     * sich zu jedem beliebigen Zeitpunkt erfragen, ob das <code>Raum</code>-Objekt steht, nicht jedoch - wie durch <code>StehReagierbar</code> -
     * am genauen Zeitpunkt des zum Stehen kommens hierauf reagieren.<br /><br />
     * Diese Methode macht natuerlich nur dann sinn, wenn sie an einem <i>Aktiv-Objekt</i> ausgefuehrt wird. Andernfalls gibt es eine Fehlermeldung!
     * @return  <code>true</code>, wenn dieses <code>Raum</code>-Objekt als <i>Aktiv-Objekt</i> auf einem Passiv-Objekt steht. Steht dieses
     * <code>Raum</code>-Objekt als <i>Aktiv-Objekt</i> nicht auf einem Passiv-Objekt, oder ist dieses <code>Raum</code>-Objekt kein
     * <i>Aktiv-Objekt</i>, so ist die Rueckgabe <code>false</code>.
     */
    public boolean steht() {
        return phClient.steht();
    }

    /**
     * <b>Bewegt</b> dieses <code>Raum</code>-Objekt. Der Unterschied zum <b>Verschieben</b> ist folgender:<br />
     * Ist dieses Objekt in der Physik beteiligt, so ist dies eine Bewegung innerhalb der Physik und kein Stumpfes Verschieben.<br />
     * Ist dieses Objekt fuer die Physik neutral ist dies genauso wie <code>verschieben</code>.
     * @param v Die Bewegung beschreibender Vektor
     * @see bewegen(int, int)
     * @return <code>true</code>, wenn sich dieses <code>Raum</code>-Objekt ohne Probleme bewegen liess. Konnte es wegen der Physik
     * (Aktiv-Objekt von Passiv-Objekt geblockt) <b>nicht vollstaendig verschoben werden</b>, so wird <code>false</code> zurueckgegeben.<br />
     * Die Rueckgabe ist bei Passiv-Objekten und neutralen Objekten immer <code>true</code>, da diese Problemlos verschoben werden können.
     */
    public boolean bewegen(Vektor v) {
        synchronized(this) {
             return phClient.bewegen(v);
        }
    }

    /**
     * <b>Bewegt</b> dieses <code>Raum</code>-Objekt. Der Unterschied zum <b>Verschieben</b> ist folgender:<br />
     * Ist dieses Objekt in der Physik beteiligt, so ist dies eine Bewegung innerhalb der Physik und kein Stumpfes Verschieben.<br />
     * Ist dieses Objekt fuer die Physik neutral ist dies genauso wie <code>verschieben</code>.
     * @param   dX  Der X-Anteil der Verschiebung (Delta-X)
     * @param   dY  Der Y-Anteil der Verschiebung (Delta-Y)
     * @see bewegen(Vektor)
     */
    public void bewegen(int dX, int dY) {
        phClient.bewegen(new Vektor(dX, dY));
    }
    
    /**
     * Setzt die Sichtbarkeit des Objektes.
     * @param   sichtbar    Ob das Objekt sichtbar sein soll oder nicht.<br />
     * Ist dieser Wert <code>false</code>, so wird es nicht im Fenster gezeichnet.<br />
     * <b>Aber:</b> Es existiert weiterhin ohne Einschraenkungen. <b>Allerdings</b> gilt ein Treffer mit einem unsichtbaren 
     * Raum-Objekt in der Klasse <code>Physik</code> nicht als Kollision. Unsichtbare Raum-Objekte werden somit bei Trefferkollisionen ausgelassen.
     * @see sichtbar()
     * @see ea.physics.Physik
     */
    public final void sichtbarSetzen(boolean sichtbar) {
        this.sichtbar = sichtbar;
    }
    
    /**
     * Gibt an, ob das Raum-Objekt sichtbar ist.
     * @return  Ist <code>true</code>, wenn das Raum-Objekt zur Zeit sichtbar ist.
     * @see sichtbarSetzen(boolean)
     */
    public final boolean sichtbar() {
        return this.sichtbar;
    }
    
    /**
     * Diese Methode ordnet einem String ein Color-Objekt zu.<br />
     * Hierdurch ist in den Klassen ausserhalb der Engine keine awt-Klasse noetig.
     * @param   t   Der Name der Farbe.<br />
     *              Ein Katalog mit allen moeglichen Namen findet sich im <b>Handbuch</b>
     * @return  Das Farbobjekt zum String; ist Color.black bei unzuordnembaren String
     */
    public static final Color zuFarbeKonvertieren(String t) {
        Color c = Color.black;
        t = t.toLowerCase(Locale.ENGLISH);
        if(t.compareTo("gelb") == 0) {
            c = Color.yellow;
        }
        else if(t.compareTo("weiss") == 0) {
            c = Color.white;
        }
        else if(t.compareTo("orange") == 0){
            c = Color.orange;
        }
        else if(t.compareTo("grau") == 0) {
            c = Color.gray;
        }
        else if(t.compareTo("gruen") == 0) {
            c = Color.green;
        }
        else if(t.compareTo("blau") == 0){
            c = Color.blue;
        }
        else if(t.compareTo("rot") == 0){
            c = Color.red;
        }
        else if(t.compareTo("pink") == 0){
            c = Color.pink;
        }
        else if(t.compareTo("magenta") == 0 ||  t.compareTo("lila") == 0){
            c = Color.magenta;
        }
        else if(t.compareTo("cyan") == 0 || t.compareTo("tuerkis") == 0){
            c = Color.cyan;
        }
        else if(t.compareTo("dunkelgrau") == 0) {
            c = Color.darkGray;
        }
        else if(t.compareTo("hellgrau") == 0){
            c = Color.lightGray;
        }
        return DateiManager.ausListe(c);
    }
    
    /**
     * Erstellt eine Halbdurchsichtige Farbe mit den selben RGB-Werten, wie die eingegebene.<br />
     * Diese Methode wird intern verwendet.
     * @param   c   Die Farbe, deren im Alphawert gesenkte Instanz erstellt werden soll.
     */
    public static final Color halbesAlpha(Color c) {
        return DateiManager.ausListe(new Color(c.getRed(), c.getGreen(), c.getBlue(), 178));
    }
    
    /**
     * Meldet ein Leuchtend-Objekt an dem vorgesehenen LeuchtErsteller Objekt an.<br />
     * Diese Methode wird dafuer vorgesehen, dass sie <b>nur im Konstruktor der dieses Interface implementierenden Instanz aufgerufen wird</b>, und zwar mit dem 
     * <code>this</code>-Pointer, sprich:<br />
     * <code>super.leuchterAnmelden(this);</code><br />
     * Prinzipiell sollte diese Methode nur innerhalb der Engine aufgerufen werden
     * @param   l   Der anzumeldende Leuchter
     */
    protected final void leuchterAnmelden(Leuchtend l) {
        macher.add(l);
    }
    
    /**
     * Meldet ein Leuchtend-Objekt an dem vorgesehenen LeuchtMacher-Objekt ab.<br />
     * Prinzipiell sollte diese Methode nur innerhalb der Engine aufgerufen werden
     * @param   l   Der abzumeldende Leuchter
     */
    protected final void leuchterAbmelden(Leuchtend l) {
        macher.entfernen(l);
    }
    
    /**
     * Interne Testmethode, die ein mathematisch simples Konzept hat.<br />
     * Es gibt kein Problem, wenn die Zahlen dasselbe Vorzeichen haben, oder wenn eine der beiden Zahlen gleich 0 ist.
     * @return  Ob diese Zahlenkonstellation ein Problem ist.
     */
    protected static final boolean problem(int z1, int z2) {
        if(z1 == 0 || z2 == 0) {
            return false;
        }
        return (z1 < 0 ^ z2 < 0);
    }
    
    /**
     * Die Basiszeichenmethode.<br />
     * Sie schliesst eine Fallabfrage wegen der Sichtbarkeit ein. Diese Methode wird bei den einzelnen Gliedern eines Knotens aufgerufen.
     * @param   g   Das zeichnende Graphics-Objekt
     * @param   r    Das BoundingRechteck, dass die Kameraperspektive Repraesentiert.<br />
     *                         Hierbei soll zunaechst getestet werden, ob das Objekt innerhalb der Kamera liegt, und erst dann gezeichnet werden.
     * @see zeichnen(java.awt.Graphics, ea.graphic.geo.BoundingRechteck )
     */
    public final void zeichnenBasic(java.awt.Graphics g, BoundingRechteck r) {
        statisch = (r.x==0)&&(r.y==0);
        if(sichtbar) {
            zeichnen(g, r);
        }
    }
    
    /**
     * Setzt die Position des Objektes gaenzlich neu auf der Zeichenebene.<br />
     * Hierbei wird die abstrakte Methode verschieben() und dimension() angewandt, um eine zur
     * vorherigen Position relative Verschiebung zu machen, die an die gewuenschte Zielposition faert.<br />
     * <b>ACHTUNG !!!</b>
     * Bei den ALLEN Objekten ist die eingegebene Position die Links oben liegende Ecke des die Figur
     * optimal umschreibenden zu den Fensterbegrenzungen parallelen Rechtecks.<br />
     * Das heisst, dass bei Kreisen zum Beispiel <b>nicht</b> die des Mittelpunktes
     * ist! Hierfuer gibt es die Sondermethode <code>mittelpunktSetzen(int x, int y)</code>.
     * @param   x   Die neue X-Koordinate
     * @param   y   Die neue Y-Koordinate
     * @see mittelpunktSetzen(int, int)
     * @see positionSetzen(Punkt)
     */
    public void positionSetzen(int x, int y) {
        BoundingRechteck r = this.dimension();
        this.verschieben(new Vektor(x-r.x, y-r.y));
    }
    
    /**
     * Verschiebt die Raum-Figur so, dass ihr Mittelpunkt die eingegebenen Koordinaten hat.<br />
     * Diese Methode Arbeitet nach dem Mittelpunkt des das Objekt abdeckenden BoundingRechtecks durch den Aufruf 
     * der Methode <code>zentrum()</code>. Daher ist diese Methode im Anwand auf ein Knoten-Objekt nicht unbedingt sinnvoll.
     * @param   x   Die X-Koordinate des neuen Mittelpunktes des Objektes
     * @param   y   Die Y-Koordinate des neuen Mittelpunktes des Objektes
     * @see mittelpunktSetzen(Punkt)
     * @see verschieben(Vektor) 
     * @see positionSetzen(int, int)
     * @see zentrum()
     */
    public void mittelpunktSetzen(int x, int y) {
        Punkt p = this.zentrum();
        this.verschieben(new Vektor(x - p.x, y - p.y));
    }
    /**
     * Verschiebt die Raum-Figur so, dass ihr Mittelpunkt die eingegebenen Koordinaten hat.<br />
     * Diese Methode Arbeitet nach dem Mittelpunkt des das Objekt abdeckenden BoundingRechtecks durch den Aufruf 
     * der Methode <code>zentrum()</code>. Daher ist diese Methode im Anwand auf ein Knoten-Objekt nicht unbedingt sinnvoll.<br />
     * Macht dasselbe wie <code>mittelPunktSetzen(p.x, p.y)</code>.
     * @param   p   Der neue Mittelpunkt des Raum-Objekts
     * @see mittelpunktSetzen(int, int)
     * @see verschieben(Vektor) 
     * @see positionSetzen(int, int)
     * @see zentrum()
     */
    public void mittelpunktSetzen(Punkt p) {
        this.mittelpunktSetzen(p.x, p.y);
    }
    
    /**
     * Die selbe Methode wie <code>positionSetzen(int x, int y)</code>, nur mit einem einzigen
     * <code>Punkt</code>-Argument. Der Algorythmus ist exakt der gleiche.
     * @param   p   Der neue Zielpunkt
     * @see     positionSetzen(int, int)
     */
    public void positionSetzen(Punkt p) {
        this.positionSetzen(p.x, p.y);
    }
    
    /**
     * Methode zum schnellen Herausfinden der Position des Raum-Objektes.<br />
     * <b>Achtung:</b> Diese Methode gibt nur die Position der <b>linken, oberen Ecke</b> aus fuer mehr Informationen 
     * ist die Methode <code>dimension()</code> zu empfehlen, die mehr Information bietet.
     * @return  Die Koordinaten des Punktes der linken, oberen Ecke in Form eines <code>Punkt</code>-Objektes
     * @see dimension()
     */
    public Punkt position() {
        BoundingRechteck b = this.dimension();
        return new Punkt(b.x, b.y);
    }
    
    /**
     * Methode zum schnellen Herausfinden des Mittelpunktes des Raum-Objektes.
     * @return  Die Koordinaten des Mittelpunktes des Objektes
     * @see dimension()
     * @see position()
     */
    public Punkt mittelPunkt() {
        BoundingRechteck b = this.dimension();
        return new Punkt(b.x + (b.breite/2), b.y + (b.hoehe/2));
    }

    /**
     * Einfache Methode, die die X-Koordinate der linken oberen 
     * Ecke des das <code>Raum</code>-Objekt exakt umrandenden <code>BoundingRechteck</code>'s auf der Zeichenebene zurueckgibt.
     * @return  Die die X-Koordinate der linken oberen Ecke auf der Zeichenebene
     */
    public int positionX() {
        return this.dimension().x;
    }

    /**
     * Einfache Methode, die die Y-Koordinate der linken oberen
     * Ecke des das <code>Raum</code>-Objekt exakt umrandenden <code>BoundingRechteck</code>'s auf der Zeichenebene zurueckgibt.
     * @return  Die die Y-Koordinate der linken oberen Ecke auf der Zeichenebene
     */
    public int positionY() {
        return this.dimension().y;
    }

    /**
     * Verschiebt das Objekt.
     * @param   v   Der Vektor, der die Verschiebung des Objekts angibt.
     * @see Vektor
     * @see verschieben(int, int)
     */
    public abstract void verschieben(Vektor v);
    
    /**
     * Verschiebt das Objekt.<br />
     * Hierbei wird nichts anderes gemacht, als <code>verschieben(new Vektor(dX, dY))</code> 
     * auszufuehren. Insofern ist diese Methode dafuer gut, sich nicht mit der Klasse Vektor
     * auseinandersetzen zu muessen.
     * @param   dX   Die Verschiebung in Richtung X
     * @param   dY   Die Verschiebung in Richtung Y
     * @see     verschieben(Vektor)
     */
    public void verschieben(int dX, int dY) {
        this.verschieben(new Vektor(dX, dY));
    }
    
    /**
     * Test, ob ein anderes Raum-Objekt von diesem geschnitten wird.
     * @param   r   Das Objekt, das auf Kollision mit diesem getestet werden soll.
     * @return  TRUE, wenn sich beide Objekte schneiden.
     */
    public abstract boolean schneidet(Raum r);
    
    /**
     * Zeichnet das Objekt.
     * @param   g   Das zeichnende Graphics-Objekt
     * @param   r    Das BoundingRechteck, dass die Kameraperspektive Repraesentiert.<br />
     *                         Hierbei soll zunaechst getestet werden, ob das Objekt innerhalb der Kamera liegt, und erst dann gezeichnet werden.
     */
    public abstract void zeichnen(java.awt.Graphics g, BoundingRechteck r);
    
    /**
     * Prueft, ob ein bestimmter Punkt innerhalb des Raum-Objekts liegt.
     * @param   p   Der Punkt, der auf Inhalt im Objekt getestet werden soll.
     * @return  TRUE, wenn der Punkt innerhalb des Objekts liegt.
     */
    public final boolean beinhaltet(Punkt p) {
        if(statisch) {
            BoundingRechteck b = Fenster.instanz().getCam().position();
            p = p.verschobeneInstanz(new Vektor(-b.x, -b.y));
        }
        BoundingRechteck[] dim = flaechen();
        for(int i = 0; i < dim.length; i++) {
            if(dim[i].istIn(p)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Methode zum Beschreiben der rechteckigen Flaeche, die dieses Objekt einnimmt.<br />
     * Diese Methode wird zentral fuer die Trefferkollisionen innerhalb der Engine benutzt und gehoert zu den wichtigsten Methoden der Klasse und der Engine.
     * @return  Ein BoundingRechteck mit dem minimal noetigen Umfang, um das Objekt <b>voll einzuschliessen</b>.
     */
    public abstract BoundingRechteck dimension();

    /**
     * Berechnet exakter alle Rechteckigen Flaechen, auf denen dieses Objekt liegt.<br />
     * Diese Methode wird von komplexeren Gebilden, wie geometrischen oder Listen ueberschrieben.
     * @return Alle Rechtecksflaechen, auf denen dieses Objekt liegt.
     * Ist standartisiert ein Array der Groesse 1 mit der <code>dimension()</code> als Inhalt.
     * @see Knoten
     */
    public BoundingRechteck[] flaechen() {
        return new BoundingRechteck[] {
            this.dimension()
        };
    }

    /**
     * Berechnet, ob dieses Raum-Objekt <b>exakt ueber einem zweiten steht</b>.<br />
     * Dies waere fuer die Engine ein Stehen auf diesem.
     * @param   m   Das Raum-Objekt, fuer das getestet werden soll, ob dieses auf ihm steht,
     * @return  <code>true</code>, wenn dieses Objekt auf dem eingegeben steht, sonst <code>false</code>
     */
    public boolean stehtAuf(Raum m) {
        return this.dimension().stehtAuf(m.dimension());
    }
    
    /**
     * Berechnet das Zentrum des Raum-Objekts als Punkt auf der Zeichenebene.<br />
     * Das Zentrum wird ueber die Methode <code>dimension()</code> berehcnet, und zwar ueber die Methode des resultierenden BoundingRechtecks.<br />
     * <br />
     * <code>dimension().zentrum()</code><br />
     * <br />
     * So erhaelt man das Zentrum dieses Raumobjekts
     * @return  Ein Punkt-Objekt mit den Koordinaten des aktuellen Zentrums des Raum-Objekts.
     */
    public Punkt zentrum() {
        return this.dimension().zentrum();
    }
    
    /**
     * Berechnet den Hoehenunterschied zwischen dem Fuss des hoeheren und dem Kopf des tieferen Raum-Objekts.
     * @param   m   Das Raum-Objekt, dessen Hoehenunterschied zu diesem gefunden werden soll
     * @return  Der <b>absolute (also niemals negative)</b> Unterschied in der Hoehe zwiscchen den beiden Objekten. <b>Ueberlagern sie sich, so ist der Rueckgabewert 0</b>!
     */
    public int hoehenUnterschied(Raum m) {
        return this.dimension().hoehenUnterschied(m.dimension());
    }
    
    /**
     * Diese Methode loescht alle eventuell vorhandenen Referenzen innerhalb der Engine auf dieses Objekt, damit es problemlos geloescht werden kann.<br />
     * <b>Achtung:</b> zwar werden hierdurch alle Referenzen geloescht, die <b>nur innerhalb</b> der Engine liegen (dies betrifft vor allem Animationen etc), jedoch nicht die 
     * innerhalb eines <code>Knoten</code>-Objektes!!!!!!!!!<br />
     * Das heisst, wenn das Objekt an einem Knoten liegt (was <b>immer der Fall ist, wenn es auch gezeichnet wird (siehe die Wurzel des Fensters)</b>), muss es trotzdem 
     * selbst geloescht werden, <b>dies erledigt diese Methode nicht!!</b>.
     */
    public void loeschen() {
        animationsManager.animationBeendenVon(this);
    }

    /**
     * Prueft, ob dieses Raum-Objekt in ener bestimmten festen Flaeche ist.
     * @param r Die kritische Flaeche, auf deren schneiden mit diesem Raum-Objekt getestet werden soll.
     * @return  <code>true</code>, wenn dieses Raum-Objekt sich mit dem BoundingRechteck schneidet, sonst <code>false</code>
     */
    public boolean inFlaeche(BoundingRechteck r) {
        BoundingRechteck[] fl = this.flaechen();
        for(int i = 0; i < fl.length; i++) {
            if(fl[i].schneidetBasic(r)) {
                return true;
            }
        }
        return false;
    }
}
