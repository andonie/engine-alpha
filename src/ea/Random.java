package ea;

import ea.internal.ano.API;
import ea.internal.ano.NoExternalUse;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Diese Klasse liefert Methoden, die <b>zufällig verteilte Rückgaben</b> haben.
 */
@API
public class Random {
    /**
     * Privater Konstruktor.
     */
    @NoExternalUse
    private Random() {
        // Es sollen keine Instanzen dieser Klasse erstellt werden.
    }

    /**
     * Gibt einen <b>zufälligen</b> <code>boolean</code>-Wert zurück.<br /> Die Wahrscheinlichkeiten für
     * <code>true</code> bzw. <code>false</code> sind gleich groß.
     *
     * @return Mit 50% Wahrscheinlichkeit <code>false</code>, mit 50% Wahrscheinlichkeit
     * <code>true</code>.
     */
    @API
    public static boolean nextBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    /**
     * Gibt einen <b>zufälligen</b> <code>int</code>-Wert zwischen <code>0</code> und einer festgelegten Obergrenze
     * zurück.<br /> Die Wahrscheinlichkeiten für die Werte zwischen
     * <code>0</code> und der Obergrenze sind gleich groß.
     *
     * @param upperLimit Die höchste Zahl, die im Ergebnis vorkommen kann.
     *
     * @return Eine Zahl <code>getX</code>, wobei <code>0 <= getX <= upperLimit</code> gilt. Die Wahrscheinlichkeit für
     * alle möglichen Rückgaben ist <i>gleich groß</i>.
     */
    @API
    public static int nextInteger(int upperLimit) {
        if (upperLimit < 0) {
            throw new IllegalArgumentException(
                    "Achtung! Für eine Zufallszahl muss die definierte Obergrenze (die inklusiv in der Ergebnismenge ist) eine nichtnegative Zahl sein!"
            );
        }

        return ThreadLocalRandom.current().nextInt(upperLimit + 1);
    }

    /**
     * Gibt einen <b>zufälligen</b> <code>float</code>-Wert im Intervall <code>[0;1)</code> zurück. Die
     * Wahrscheinlichkeit ist für alle möglichen Werte in diesem Intervall gleich groß.
     *
     * @return Ein <code>float</code>Wert im Intervall <code>[0;1]</code>. Die Wahrscheinlichkeit für alle möglichen
     * Rückgaben ist <i>gleich groß</i>.
     */
    @API
    public static float nextFloat() {
        return ThreadLocalRandom.current().nextFloat();
    }
}
