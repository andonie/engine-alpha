package ea.input;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Eine Frage ist ein modaler Dialog, der eine Frage zwischen "OK" und "Abbrechen" ausgibt.<br />
 * Verwendet werden sollte allerdings dies ueber die Klasse Fenster, folgendermassen:
 * <b>Beispiel:</b><br /><br />
 * <code>
 * //Das instanziierte Fenster<br />
 * Fenster fenster;<br />
 * <br />
 * //Senden einer Fensternachricht mit boolean rueckgabe<br />
 * boolean istOK = fenster.frage("Wollen sie das Programm beenden?");<br />
 * if(istOK) {<br />
 * <tab />fenster.beenden();<br />
 * }<br />
 * else {<br />
 * //Nichts<br />
 * }<br />
 * </code><br />
 * @author Andonie
 */
public class Frage
extends JDialog {
    
    /**
     * Das Ergebnis der Frage.
     */
    public static boolean ergebnis;

    /**
     * Der Konstruktor. Erstellt das Objekt und setzt es sichtbar.
     * @param   parent  Das noetioge Fenster-Parent-Objekt
     * @param   frage   Die Frage, die im Dialog gezeigt wird.
     * @param janein Ob Ja-Nein zur Auswahl stehen soll oder Ok-Abbrechen
     * @param font Der Font, in dem die Texte dargestellt werden.
     */
    public Frage(Frame parent, String frage, boolean janein, Font font) {
        super(parent, true);
        ergebnis = false;
        setLayout(new BorderLayout());
        Dimension screenSize = getToolkit().getScreenSize();
        this.setLocation(screenSize.width / 4, screenSize.height / 4);
        JLabel l = new JLabel(frage);
        l.setFont(font);
        getContentPane().add(l, BorderLayout.CENTER);
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton b;
        JButton d;
        if(janein) {
            b = new JButton("Ja");
            d = new JButton("Nein");
        } else {
            b = new JButton("OK");
            d = new JButton("Abbrechen");
        }
        b.setFont(font);
        d.setFont(font);
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ergebnis = true;
                dispose();
            }
        });
        p.add(b);
        d.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ergebnis = false;
                dispose();
            }
        });
        p.add(d);
        getContentPane().add(p, BorderLayout.SOUTH);
        try{
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            SwingUtilities.updateComponentTreeUI(this);
        }catch(Exception e) {}
        pack();
        setVisible(true);
    }
}
