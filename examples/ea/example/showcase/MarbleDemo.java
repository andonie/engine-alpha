package ea.example.showcase;

import ea.*;
import ea.actor.Circle;
import ea.actor.Rectangle;
import ea.handle.BodyType;
import ea.input.KeyListener;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Eine kleine Demo zum Verhalten vieler partikelähnlicher Physik-Objekte in der Engine.
 * <p>
 * Created by Michael on 11.04.2017.
 */
public class MarbleDemo
        extends ShowcaseDemo
        implements KeyListener {

    /**
     * Konstanten zur Beschreibung der Position des Trichters.
     */
    private static final int ABSTAND_OBEN = 300, ABSTAND_LINKS = 40, ABSTAND_RECHTS = 470;

    /**
     * Der Boden des Trichters. Kann durchlässig gemacht werden.
     */
    private Rectangle boden;

    public MarbleDemo(Scene parent) {
        super(parent);

        initialisieren();

        getKeyListeners().add(this);
        getFrameUpdateListeners().add(new PeriodicTask(100, () -> {
            Circle marble = makeAMarble();
            add(marble);
            marble.setBodyType(BodyType.DYNAMIC);
            marble.position.set(new Vector(ABSTAND_LINKS + 200, ABSTAND_OBEN - 150));
            marble.physics.applyImpulse(new Vector(Random.nextFloat() * 200 - 100, Random.nextFloat() * -300 - 100));
        }));
    }

    public void initialisieren() {

        //Trichter
        Rectangle lo = new Rectangle(50, 150);
        lo.position.set(ABSTAND_LINKS, ABSTAND_OBEN);
        Rectangle lm = new Rectangle(50, 200);
        lm.position.set(ABSTAND_LINKS, ABSTAND_OBEN + 150);
        Rectangle ro = new Rectangle(50, 150);
        ro.position.set(ABSTAND_RECHTS, ABSTAND_OBEN);
        Rectangle rm = new Rectangle(50, 200);
        rm.position.set(ABSTAND_RECHTS + 14, ABSTAND_OBEN + 120);
        Rectangle lu = new Rectangle(50, 120);
        lu.position.set(ABSTAND_LINKS + 125, ABSTAND_OBEN + 255);
        Rectangle ru = new Rectangle(50, 120);
        ru.position.set(ABSTAND_LINKS + 304, ABSTAND_OBEN + 260);

        boden = new Rectangle(230, 40);
        boden.position.set(ABSTAND_LINKS + 125, ABSTAND_OBEN + 375);

        Rectangle[] allRectangles = new Rectangle[] {
                lo, lm, ro, rm, lu, ru, boden
        };

        for (Rectangle r : allRectangles) {
            r.setColor(Color.WHITE);
            add(r);
            r.setBodyType(BodyType.STATIC);
        }

        lm.getLayer().getParent().setGravity(new Vector(0, 15));

        lm.position.setRotation(-(float) Math.PI / 4);
        rm.position.setRotation((float) Math.PI / 4);
    }

    @Override
    public void onKeyDown(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_X: // Boden togglen
                if (boden.getBodyType() == BodyType.STATIC) {
                    boden.setBodyType(BodyType.PASSIVE);
                    boden.setColor(new Color(255, 255, 255, 100));
                } else {
                    boden.setBodyType(BodyType.STATIC);
                    boden.setColor(new Color(255, 255, 255));
                }
                break;
        }
    }

    @Override
    public void onKeyUp(KeyEvent e) {
        // Ignore.
    }

    /**
     * Erstellt eine neue Murmel.
     *
     * @return eine Murmel. Farbe und Größe variieren.
     */
    public Circle makeAMarble() {

        class Marble extends Circle implements FrameUpdateListener {

            public Marble(float diameter) {
                super(diameter);
            }

            @Override
            public void onFrameUpdate(float i) {
                if (this.position.getCenter().getLength() > 1000) {
                    MarbleDemo.this.remove(this);
                }
            }
        }

        Circle murmel = new Marble(Random.nextInteger(50) + 10);
        murmel.setBodyType(BodyType.DYNAMIC);
        murmel.physics.setMass(4);
        murmel.setColor(new Color(
                Random.nextInteger(255), Random.nextInteger(255), Random.nextInteger(255)));

        return murmel;
    }
}
