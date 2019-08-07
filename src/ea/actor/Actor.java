/*
 * Engine Alpha ist eine anfängerorientierte 2D-Gaming Engine.
 *
 * Copyright (c) 2011 - 2014 Michael Andonie and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ea.actor;

import ea.*;
import ea.collision.CollisionListener;
import ea.event.*;
import ea.internal.ShapeBuilder;
import ea.internal.annotations.API;
import ea.internal.annotations.Internal;
import ea.internal.physics.NullHandler;
import ea.internal.physics.PhysicsData;
import ea.internal.physics.PhysicsHandler;
import ea.internal.physics.WorldHandler;
import ea.internal.util.Logger;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.joints.DistanceJointDef;
import org.jbox2d.dynamics.joints.RevoluteJointDef;
import org.jbox2d.dynamics.joints.RopeJointDef;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Actor bezeichnet alles, was sich auf der Zeichenebene befindet.<br> Dies ist die absolute Superklasse aller
 * grafischen Objekte. Umgekehrt kann somit jedes grafische Objekt die folgenden Methoden nutzen.
 *
 * @author Michael Andonie
 * @author Niklas Keller
 */
@SuppressWarnings ( "OverlyComplexClass" )
public abstract class Actor implements KeyListenerContainer, MouseClickListenerContainer, MouseWheelListenerContainer, FrameUpdateListenerContainer {
    private <T> Supplier<T> createParentSupplier(Function<Layer, T> supplier) {
        return () -> {
            Layer layer = getLayer();
            if (layer == null) {
                return null;
            }

            return supplier.apply(layer);
        };
    }

    /**
     * Gibt an, ob das Objekt zur Zeit überhaupt sichtbar sein soll.<br> Ist dies nicht der Fall, so wird die
     * Zeichenroutine direkt übergangen.
     */
    private boolean visible = true;

    /**
     * Z-Index des Raumes, je höher, desto weiter oben wird der Actor gezeichnet
     */
    private int layerPosition = 1;

    /**
     * Opacity = Durchsichtigkeit des Raumes
     * <p>
     * <ul><li><code>0.0f</code> entspricht einem komplett durchsichtigen Image.</li>
     * <li><code>1.0f</code> entspricht einem undurchsichtigem Image.</li></ul>
     */
    private float opacity = 1;

    /**
     * Der JB2D-Handler für dieses spezifische Objekt.
     */
    private PhysicsHandler physicsHandler;

    private final Object physicsHandlerLock = new Object();

    private final EventListeners<Runnable> mountListeners = new EventListeners<>();
    private final EventListeners<Runnable> unmountListeners = new EventListeners<>();
    private final EventListeners<KeyListener> keyListeners = new EventListeners<>(createParentSupplier(Layer::getKeyListeners));
    private final EventListeners<MouseClickListener> mouseClickListeners = new EventListeners<>(createParentSupplier(Layer::getMouseClickListeners));
    private final EventListeners<MouseWheelListener> mouseWheelListeners = new EventListeners<>(createParentSupplier(Layer::getMouseWheelListeners));
    private final EventListeners<FrameUpdateListener> frameUpdateListeners = new EventListeners<>(createParentSupplier(Layer::getFrameUpdateListeners));

    /* _________________________ Die Handler _________________________ */

    public Actor(Supplier<Shape> shapeSupplier) {
        this.physicsHandler = new NullHandler(new PhysicsData(() -> Collections.singletonList(shapeSupplier.get())));
        EventListenerHelper.autoRegisterListeners(this);
    }

    @API
    public final void addMountListener(Runnable listener) {
        synchronized (physicsHandlerLock) {
            mountListeners.add(listener);

            if (physicsHandler.getWorldHandler() != null) {
                listener.run();
            }
        }
    }

    @API
    public final void removeMountListener(Runnable listener) {
        synchronized (physicsHandlerLock) {
            mountListeners.remove(listener);
        }
    }

    @API
    public final void addUnmountListener(Runnable listener) {
        synchronized (physicsHandlerLock) {
            unmountListeners.add(listener);
        }
    }

    @API
    public final void removeUnmountListener(Runnable listener) {
        synchronized (physicsHandlerLock) {
            unmountListeners.remove(listener);
        }
    }

    /* _________________________ Getter & Setter (die sonst nicht zuordbar) _________________________ */

    /**
     * Setzt den Layer dieses Actors. Je größer, desto weiter vorne wird ein Actor gezeichnet.
     * <b>Diese Methode muss ausgeführt werden, bevor der Actor zu einer ActorGroup hinzugefügt
     * wird.</b>
     *
     * @param position Layer-Index
     */
    @API
    public void setLayerPosition(int position) {
        this.layerPosition = position;
    }

    /**
     * Gibt den Layer zurück.
     *
     * @return Layer-Index
     */
    @API
    public int getLayerPosition() {
        return this.layerPosition;
    }

    /**
     * Setzt die Sichtbarkeit des Objektes.
     *
     * @param visible Ob das Objekt isVisible sein soll oder nicht.<br> Ist dieser Wert
     *                <code>false</code>, so wird es nicht im Window gezeichnet.
     *
     * @see #isVisible()
     */
    @API
    public final void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Gibt an, ob das Actor-Objekt isVisible ist.
     *
     * @return Ist <code>true</code>, wenn das Actor-Objekt zur Zeit isVisible ist.
     *
     * @see #setVisible(boolean)
     */
    @API
    public final boolean isVisible() {
        return this.visible;
    }

    /**
     * Gibt die aktuelle Opacity des Raumes zurück.
     *
     * @return Gibt die aktuelle Opacity des Raumes zurück.
     */
    @API
    public float getOpacity() {
        return opacity;
    }

    /**
     * Setzt die Opacity des Raumes.
     * <p>
     * <ul><li><code>0.0f</code> entspricht einem komplett durchsichtigen Actor.</li>
     * <li><code>1.0f</code> entspricht einem undurchsichtigem Actor.</li></ul>
     */
    @API
    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    /* _________________________ API-Methoden in der Klasse direkt _________________________ */

    /**
     * Prueft, ob ein bestimmter Point innerhalb des Actor-Objekts liegt.
     *
     * @param p Der Point, der auf Inhalt im Objekt getestet werden soll.
     *
     * @return TRUE, wenn der Point innerhalb des Objekts liegt.
     */
    @API
    public final boolean contains(Vector p) {
        synchronized (physicsHandlerLock) {
            return physicsHandler.contains(p);
        }
    }

    /**
     * Prueft, ob dieser Actor sich mit einem weiteren Actor schneidet.<br> Für die Überprüfung des Überlappens werden
     * die internen <b>Collider</b> genutzt. Je nach Genauigkeit der Collider kann die Überprüfung unterschiedlich
     * befriedigend ausfallen. Die Collider können im <b>Debug-Modus</b> der Engine eingesehen werden.
     *
     * @param other Ein weiteres Actor-Objekt.
     *
     * @return <code>true</code>, wenn dieses Actor-Objekt sich mit <code>another</code> schneidet. Sonst
     * <code>false</code>.
     *
     * @see ea.Game#setDebug(boolean)
     */
    @API
    public final boolean overlaps(Actor other) {
        synchronized (physicsHandlerLock) {
            return WorldHandler.isBodyCollision(physicsHandler.getBody(), other.getPhysicsHandler().getBody());
        }
    }

    /* _________________________ Utilities, interne & überschriebene Methoden _________________________ */

    /**
     * Setzt, was für eine Type physikalisches Objekt das Objekt sein soll. Erläuterung findet
     * sich im <code>enum Type</code>.
     *
     * @param type Der Type Physics-Objekt, der ab sofort dieses Objekt sein soll.
     *
     * @see BodyType
     */
    @API
    public void setBodyType(BodyType type) {
        synchronized (physicsHandlerLock) {
            this.physicsHandler.setType(type);
        }
    }

    /**
     * Gibt aus, was für ein Type Physics-Objekt dieses Objekt momentan ist.
     *
     * @return der Type Physics-Objekt, der das entsprechende <code>Actor</code>-Objekt momentan ist.
     *
     * @see BodyType
     */
    @API
    public BodyType getBodyType() {
        synchronized (physicsHandlerLock) {
            return physicsHandler.getType();
        }
    }

    /**
     * Setzt neue Shapes für das Actor Objekt. Hat Einfluss auf die Physik (Kollisionen, Masse, etc.)
     *
     * @param shapeCode der Shape-Code
     *
     * @see ShapeBuilder#fromString(String)
     * @see #setShape(Supplier)
     * @see #setShapes(Supplier)
     */
    @API
    public final void setShapes(String shapeCode) {
        this.setShapes(ShapeBuilder.fromString(shapeCode));
    }

    /**
     * Ändert die Shape des Actors neu in eine alternative Shape.Alle anderen physikalischen Eigenschaften bleiben
     * weitgehend erhalten.
     *
     * @param shapeSupplier Der Supplier, der die neue Shape des Objektes ausgibt.
     *
     * @see #setShapes(Supplier)
     */
    @API
    public final void setShape(Supplier<Shape> shapeSupplier) {
        this.setShapes(() -> Collections.singletonList(shapeSupplier.get()));
    }

    /**
     * Ändert die Shapes dieses Actors in eine Reihe neuer Shapes. Alle anderen physikalischen Eigenschaften bleiben
     * weitgehend erhalten.
     *
     * @param shapesSupplier Ein Supplier, der eine Liste mit allen neuen Shapes für den Actor angibt.
     *
     * @see #setShape(Supplier)
     */
    @API
    public final void setShapes(Supplier<List<Shape>> shapesSupplier) {
        synchronized (physicsHandlerLock) {
            this.physicsHandler.setShapes(shapesSupplier);
        }
    }

    /**
     * Die Basiszeichenmethode.<br> Sie schließt eine Fallabfrage zur Sichtbarkeit ein. Diese Methode wird bei den
     * einzelnen Gliedern eines Knotens aufgerufen.
     *
     * @param g Das zeichnende Graphics-Objekt
     * @param r Das Bounds, dass die Kameraperspektive Repraesentiert.<br> Hierbei soll zunaechst getestet
     *          werden, ob das Objekt innerhalb der Kamera liegt, und erst dann gezeichnet werden.
     */
    @Internal
    public void renderBasic(Graphics2D g, Bounds r, float pixelPerMeter) {
        if (visible && this.isWithinBounds(r)) {
            synchronized (physicsHandlerLock) {
                float rotation = physicsHandler.getRotation();
                Vector position = physicsHandler.getPosition();

                // ____ Pre-Render ____

                AffineTransform transform = g.getTransform();

                g.rotate(-rotation, position.x * pixelPerMeter, -position.y * pixelPerMeter);
                g.translate(position.x * pixelPerMeter, -position.y * pixelPerMeter);

                // Opacity Update
                Composite composite;
                if (opacity != 1) {
                    composite = g.getComposite();
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, opacity));
                } else {
                    composite = null;
                }

                // ____ Render ____

                render(g, pixelPerMeter);

                if (Game.isDebug()) {
                    synchronized (this) {
                        // Visualisiere die Shape
                        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                        Body body = physicsHandler.getBody();

                        if (body != null) {
                            Fixture fixture = body.m_fixtureList;
                            while (fixture != null && fixture.m_shape != null) {
                                renderShape(fixture.m_shape, g, pixelPerMeter);
                                fixture = fixture.m_next;
                            }
                        }

                        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    }
                }

                // ____ Post-Render ____

                // Opacity Update
                if (composite != null) {
                    g.setComposite(composite);
                }

                // Transform zurücksetzen
                g.setTransform(transform);
            }
        }
    }

    /**
     * Rendert eine Shape von JBox2D vectorFromThisTo den gegebenen Voreinstellungen im Graphics-Objekt.
     *
     * @param shape Die Shape, die zu rendern ist.
     * @param g     Das Graphics2D-Object, das die Shape rendern soll. Farbe &amp; Co. sollte im Vorfeld
     *              eingestellt sein. Diese Methode übernimmt nur das direkte rendern.
     */
    @Internal
    public static void renderShape(Shape shape, Graphics2D g, float pixelPerMeter) {
        if (shape == null) {
            return;
        }

        AffineTransform pre = g.getTransform();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        g.setColor(Color.YELLOW);
        g.drawLine(0, 0, 0, 0);
        g.setColor(Color.RED);

        if (shape instanceof PolygonShape) {
            PolygonShape polygonShape = (PolygonShape) shape;
            Vec2[] vec2s = polygonShape.getVertices();
            int[] xs = new int[polygonShape.getVertexCount()], ys = new int[polygonShape.getVertexCount()];
            for (int i = 0; i < xs.length; i++) {
                xs[i] = (int) (vec2s[i].x * pixelPerMeter);
                ys[i] = (-1) * (int) (vec2s[i].y * pixelPerMeter);
            }

            g.drawPolygon(xs, ys, xs.length);
        } else if (shape instanceof CircleShape) {
            CircleShape circleShape = (CircleShape) shape;
            float diameter = (circleShape.m_radius * 2);
            g.drawOval((int) ((circleShape.m_p.x - circleShape.m_radius) * pixelPerMeter), (int) ((-circleShape.m_p.y - circleShape.m_radius) * pixelPerMeter), (int) (diameter * (double) pixelPerMeter), (int) (diameter * (double) pixelPerMeter));
        } else {
            Logger.error("Debug/Render", "Konnte die Shape (" + shape + ") nicht rendern. Unerwartete Shape.");
        }

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setTransform(pre);
    }

    /**
     * Interne Methode. Prüft, ob das anliegende Objekt (teilweise) innerhalb des sichtbaren Bereichs liegt.
     *
     * @param r Die Bounds der Kamera.
     *
     * @return <code>true</code>, wenn das Objekt (teilweise) innerhalb des derzeit sichtbaren
     * Breichs liegt, sonst <code>false</code>.
     */
    @Internal
    private boolean isWithinBounds(Bounds r) {
        // FIXME : Parameter ändern (?) und Funktionalität implementieren.
        return true;
    }

    /**
     * Gibt den aktuellen, internen Physics-Handler aus.
     *
     * @return der aktuellen, internen WorldHandler-Handler aus.
     */
    @Internal
    public PhysicsHandler getPhysicsHandler() {
        return physicsHandler;
    }

    /* _________________________ Listeners _________________________ */

    /**
     * Meldet einen neuen {@link CollisionListener} an, der auf alle Kollisionen zwischen diesem Actor und dem Actor
     * <code>collider</code> reagiert.
     *
     * @param listener Der Listener, der bei Kollisionen zwischen dem <b>ausführenden Actor</b> und
     *                 <code>collider</code> informiert werden soll.
     * @param collider Ein weiteres Actor-Objekt.
     * @param <E>      Typ-Parameter. SOllte im Regelfall exakt die Klasse von <code>collider</code> sein. Dies
     *                 ermöglicht die Nutzung von spezifischen Methoden aus spezialisierteren Klassen der
     *                 Actor-Hierarchie.
     *
     * @see #addCollisionListener(CollisionListener)
     */
    @API
    public <E extends Actor> void addCollisionListener(E collider, CollisionListener<E> listener) {
        addMountListener(() -> WorldHandler.addSpecificCollisionListener(this, collider, listener));
    }

    /**
     * Meldet einen neuen {@link CollisionListener} an, der auf alle Kollisionen reagiert, die dieser Actor mit seiner
     * Umwelt erlebt.
     *
     * @param listener Der Listener, der bei Kollisionen informiert werden soll, die der  <b>ausführende Actor</b> mit
     *                 allen anderen Objekten der Scene erlebt.
     *
     * @see #addCollisionListener(Actor, CollisionListener)
     */
    @API
    public void addCollisionListener(CollisionListener<Actor> listener) {
        addMountListener(() -> WorldHandler.addGenericCollisionListener(listener, this));
    }

    /* _________________________ Kontrakt: Abstrakte Methoden/Funktionen eines Actor-Objekts _________________________ */

    /**
     * Rendert das Objekt am Ursprung. <ul> <li>Die Position ist (0|0).</li> <li>Die Roation ist 0.</li> </ul>
     *
     * @param g Das zeichnende Graphics-Objekt
     */
    @Internal
    public abstract void render(Graphics2D g, float pixelPerMeter);

    @Internal
    public void setPhysicsHandler(PhysicsHandler handler) {
        synchronized (physicsHandlerLock) {
            WorldHandler worldHandler = handler.getWorldHandler();
            WorldHandler previousWorldHandler = physicsHandler.getWorldHandler();

            if (worldHandler == null) {
                if (previousWorldHandler == null) {
                    return;
                }

                Layer layer = previousWorldHandler.getLayer();

                keyListeners.invoke(layer::removeKeyListener);
                mouseClickListeners.invoke(layer::removeMouseClickListener);
                mouseWheelListeners.invoke(layer::removeMouseWheelListener);
                frameUpdateListeners.invoke(layer::removeFrameUpdateListener);

                unmountListeners.invoke(Runnable::run);

                physicsHandler = handler;
            } else {
                if (previousWorldHandler != null) {
                    return;
                }

                physicsHandler = handler;

                Layer layer = worldHandler.getLayer();

                mountListeners.invoke(Runnable::run);

                keyListeners.invoke(layer::addKeyListener);
                mouseClickListeners.invoke(layer::addMouseClickListener);
                mouseWheelListeners.invoke(layer::addMouseWheelListener);
                frameUpdateListeners.invoke(layer::addFrameUpdateListener);
            }
        }
    }

    public Layer getLayer() {
        synchronized (physicsHandlerLock) {
            WorldHandler worldHandler = physicsHandler.getWorldHandler();
            if (worldHandler == null) {
                return null;
            }

            return worldHandler.getLayer();
        }
    }

    public void remove() {
        Layer layer = getLayer();
        if (layer != null) {
            layer.remove(this);
        }
    }

    @API
    public EventListeners<KeyListener> getKeyListeners() {
        return keyListeners;
    }

    @API
    public EventListeners<MouseClickListener> getMouseClickListeners() {
        return mouseClickListeners;
    }

    @API
    public EventListeners<MouseWheelListener> getMouseWheelListeners() {
        return mouseWheelListeners;
    }

    @API
    public EventListeners<FrameUpdateListener> getFrameUpdateListeners() {
        return frameUpdateListeners;
    }

    /**
     * Setzt, ob <i>im Rahmen der physikalischen Simulation</i> die Rotation dieses Objekts
     * blockiert werden soll. <br>
     * Das Objekt kann in jedem Fall weiterhin über einen direkten Methodenaufruf rotiert
     * werden. Der folgende Code ist immer wirksam, unabhängig davon, ob die Rotation
     * im Rahmen der physikalischen Simulation blockiert ist:<br>
     * <code>
     * actor.getPosition.rotate(4.31f);
     * </code>
     *
     * @param rotationLocked Ist dieser Wert <code>true</code>, rotiert sich dieses
     *                       Objekts innerhalb der physikalischen Simulation <b>nicht mehr</b>.
     *                       Ist dieser Wert <code>false</code>, rotiert sich dieses
     *                       Objekt innerhalb der physikalsichen Simulation.
     *
     * @see #isRotationLocked()
     */
    @API
    public void setRotationLocked(boolean rotationLocked) {
        synchronized (physicsHandlerLock) {
            physicsHandler.setRotationLocked(rotationLocked);
        }
    }

    /**
     * Gibt an, ob die Rotation dieses Objekts derzeit innerhalb der physikalischen Simulation
     * blockiert ist.
     *
     * @return <code>true</code>, wenn die Rotation dieses Objekts derzeit innerhalb der
     * physikalischen Simulation blockiert ist.
     *
     * @see #setRotationLocked(boolean)
     */
    @API
    public boolean isRotationLocked() {
        synchronized (physicsHandlerLock) {
            return physicsHandler.isRotationLocked();
        }
    }

    /**
     * Setzt die Masse des Objekts neu. Hat Einfluss auf das physikalische Verhalten des Objekts.
     *
     * @param massInKG Die neue Masse für das Objekt in <b>[kg]</b>.
     */
    @API
    public void setMass(float massInKG) {
        synchronized (physicsHandlerLock) {
            physicsHandler.setMass(massInKG);
        }
    }

    /**
     * Gibt die aktuelle Masse des Ziel-Objekts aus. Die Form bleibt unverändert, daher ändert sich
     * die <b>Dichte</b> in der Regel.
     *
     * @return Die Masse des Ziel-Objekts in <b>[kg]</b>.
     */
    @API
    public float getMass() {
        synchronized (physicsHandlerLock) {
            return physicsHandler.getMass();
        }
    }

    /**
     * Setzt die Dichte des Objekts neu. Die Form bleibt dabei unverändert, daher ändert sich die
     * <b>Masse</b> in der Regel.
     *
     * @param densityInKgProQM die neue Dichte des Objekts in <b>[kg/m^2]</b>
     */
    @API
    public void setDensity(float densityInKgProQM) {
        synchronized (physicsHandlerLock) {
            physicsHandler.setDensity(densityInKgProQM);
        }
    }

    /**
     * Gibt die aktuelle Dichte des Objekts an.
     *
     * @return Die aktuelle Dichte des Objekts in <b>[kg/m^2]</b>.
     */
    @API
    public float getDensity() {
        synchronized (physicsHandlerLock) {
            return physicsHandler.getDensity();
        }
    }

    /**
     * Setzt den Reibungskoeffizient für das Objekt. Hat Einfluss auf
     * die Bewegung des Objekts.
     *
     * @param coefficientOfElasticity Der Reibungskoeffizient. In der Regel im Bereich <b>[0; 1]</b>.
     */
    @API
    public void setFriction(float coefficientOfElasticity) {
        synchronized (physicsHandlerLock) {
            physicsHandler.setFriction(coefficientOfElasticity);
        }
    }

    /**
     * Gibt den Reibungskoeffizienten für dieses Objekt aus.
     *
     * @return Der Reibungskoeffizient des Objekts. Ist in der Regel (in der Realität)
     * ein Wert im Bereich <b>[0; 1]</b>.
     */
    @API
    public float getFriction() {
        synchronized (physicsHandlerLock) {
            return physicsHandler.getFriction();
        }
    }

    /**
     * Setzt die Geschwindigkeit "hart" für dieses Objekt. Damit wird die aktuelle
     * Bewegung (nicht aber die Rotation) des Objekts ignoriert und hart auf den
     * übergebenen Wert gesetzt.
     *
     * @param velocityInMPerS Die Geschwindigkeit, mit der sich dieses Objekt ab sofort
     *                        bewegen soll. In <b>[m / s]</b>
     */
    @API
    public void setVelocity(Vector velocityInMPerS) {
        synchronized (physicsHandlerLock) {
            physicsHandler.setVelocity(velocityInMPerS);
        }
    }

    /**
     * Gibt die Geschwindigkeit aus, mit der sich dieses Objekt gerade (also in diesem Frame) bewegt.
     *
     * @return Die Geschwindigkeit, mit der sich dieses Objekt gerade (also in diesem Frame) bewegt.
     * In <b>[m / s]</b>
     */
    @API
    public Vector getVelocity() {
        synchronized (physicsHandlerLock) {
            return physicsHandler.getVelocity();
        }
    }

    @API
    public void setRestitution(float elasticity) {
        synchronized (physicsHandlerLock) {
            physicsHandler.setRestitution(elasticity);
        }
    }

    @API
    public float getRestitution() {
        synchronized (physicsHandlerLock) {
            return physicsHandler.getRestitution();
        }
    }

    /* _________________________ Doers : Direkter Effekt auf Simulation _________________________ */

    /**
     * Wirkt eine Kraft auf den <i>Schwerpunkt</i> des Objekts.
     *
     * @param force Ein Kraft-Vector. Einheit ist <b>nicht [px], sonder [N]</b>.
     */
    @API
    public void applyForce(Vector force) {
        synchronized (physicsHandlerLock) {
            physicsHandler.applyForce(force);
        }
    }

    /**
     * Wirkt eine Kraft auf einem bestimmten <i>Point in der Welt</i>.
     *
     * @param kraftInN    Eine Kraft. Einheit ist <b>[N]</b>
     * @param globalPoint Der Ort auf der <i>Zeichenebene</i>, an dem die Kraft wirken soll.
     */
    @API
    public void applyForce(Vector kraftInN, Vector globalPoint) {
        synchronized (physicsHandlerLock) {
            physicsHandler.applyForce(kraftInN, globalPoint);
        }
    }

    /**
     * Wirkt einen Impuls auf den <i>Schwerpunkt</i> des Objekts.
     *
     * @param impulseInNS Der Impuls, der auf den Schwerpunkt wirken soll. Einheit ist <b>[Ns]</b>
     */
    @API
    public void applyImpulse(Vector impulseInNS) {
        synchronized (physicsHandlerLock) {
            physicsHandler.applyImpluse(impulseInNS, physicsHandler.getCenter());
        }
    }

    /**
     * Wirkt einen Impuls an einem bestimmten <i>Point in der Welt</i>.
     *
     * @param impulseInNS Ein Impuls. Einheit ist <b>[Ns]</b>
     * @param globalPoint Der Ort auf der <i>Zeichenebene</i>, an dem der Impuls wirken soll.
     */
    @API
    public void applyImpulse(Vector impulseInNS, Vector globalPoint) {
        synchronized (physicsHandlerLock) {
            physicsHandler.applyImpluse(impulseInNS, globalPoint);
        }
    }

    /**
     * Versetzt das Objekt - unabhängig von aktuellen Kräften und Geschwindigkeiten -
     * <i>in Ruhe</i>. Damit werden alle (physikalischen) Bewegungen des Objektes zurückgesetzt.
     * Sollte eine konstante <i>Schwerkraft</i> (oder etwas Vergleichbares) exisitieren, wo
     * wird dieses Objekt jedoch möglicherweise aus der Ruhelage wieder in Bewegung versetzt.
     */
    @API
    public void resetMovement() {
        synchronized (physicsHandlerLock) {
            physicsHandler.resetMovement();
        }
    }

    /**
     * Testet, ob das Objekt "steht". Diese Funktion ist below anderem hilfreich für die Entwicklung von Platformern
     * (z.B. wenn der Spieler nur springen können soll, wenn er auf dem Boden steht).<br>
     * Diese Funktion ist eine <b>Heuristik</b>, sprich sie ist eine Annäherung. In einer Physik-Simulation ist die
     * Definition von "stehen" nicht unbedingt einfach. Hier bedeutet es folgendes:<br>
     * <i>Ein Objekt steht genau dann, wenn alle Eigenschaften erfüllt sind:</i>
     * <ul>
     * <li>Es ist ein <b>dynamisches Objekt</b>.</li>
     * <li>Direkt below der Mitte der minimalen
     * <a href="https://en.wikipedia.org/wiki/Minimum_bounding_box#Axis-aligned_minimum_bounding_box">AABB</a>,
     * die das gesamte Objekt umspannt, befindet sich ein <b>statisches Objekt</b>.</li>
     * </ul>
     */
    @API
    public boolean isGrounded() {
        synchronized (physicsHandlerLock) {
            return physicsHandler.isGrounded();
        }
    }

    /* _________________________ JOINTS _________________________ */

    /**
     * Prüft ob das zugehörige <code>Actor</code>-Objekt in der selben JB2D World liegt wie das übergebene Objekt.
     * Diese Logik ist ausgelagert, um den Code etwas schöner zu machen.
     *
     * @param other ein zweites <code>Actor</code>-Objekt zum testen.
     */
    @Internal
    private void assertSameWorld(Actor other) {
        synchronized (physicsHandlerLock) {
            PhysicsHandler otherHandler = other.getPhysicsHandler();
            if (otherHandler.getWorldHandler() != this.physicsHandler.getWorldHandler()) {
                throw new RuntimeException("Die Actor-Objekte sind nicht an der selben Wurzel angemeldet. Sie können deshalb (noch) nicht physikalisch verbunden werden.");
            }
        }
    }

    /**
     * Erstellt einen Revolute-Joint zwischen dem zugehörigen <code>Actor</code>-Objekt und einem weiteren.
     *
     * <h3>Definition Revolute-Joint</h3>
     * <p>Verbindet zwei <code>Actor</code>-Objekte <b>untrennbar an einem Anchor-Point</b>. Die Objekte können sich
     * ab sofort nur noch <b>relativ zueinander drehen</b>.</p>
     *
     * @param other            Das zweite <code>Actor</code>-Objekt, das ab sofort mit dem zugehörigen
     *                         <code>Actor</code>-Objekt
     *                         über einen <code>RevoluteJoint</code> verbunden sein soll.
     * @param anchorAsWorldPos Der Ankerpunkt <b>auf dem Layer</b>. Es wird davon
     *                         ausgegangen, dass beide Objekte bereits korrekt positioniert sind.
     *
     * @return Ein <code>Joint</code>-Objekt, mit dem der Joint weiter gesteuert werden kann.
     *
     * @see org.jbox2d.dynamics.joints.RevoluteJoint
     */
    @API
    public Joint createRevoluteJoint(Actor other, Vector anchorAsWorldPos) {
        return createJoint(() -> {
            assertSameWorld(other);

            // Definiere den Joint
            RevoluteJointDef revoluteJointDef = new RevoluteJointDef();
            revoluteJointDef.initialize(physicsHandler.getBody(), other.getPhysicsHandler().getBody(),
                    //actor.physicsHandler.getWorldHandler().fromVektor(actor.getPosition.get().asVector().add(anchorAsWorldPos)));
                    anchorAsWorldPos.toVec2());
            revoluteJointDef.collideConnected = false;

            return physicsHandler.getWorldHandler().getWorld().createJoint(revoluteJointDef);
        });
    }

    /**
     * Erstellt einen Rope-Joint zwischen diesem und einem weiteren <code>Actor</code>-Objekt.
     *
     * @param other      Das zweite <code>Actor</code>-Objekt, das ab sofort mit dem zugehörigen
     *                   <code>Actor</code>-Objekt
     *                   über einen <code>RopeJoint</code> verbunden sein soll.
     * @param anchorA    Der Ankerpunkt für das zugehörige <code>Actor</code>-Objekt. Der erste Befestigungspunkt
     *                   des Lassos. Angabe relativ zur Position vom zugehörigen Objekt.
     * @param anchorB    Der Ankerpunkt für das zweite <code>Actor</code>-Objekt, also <code>other</code>.
     *                   Der zweite Befestigungspunkt des Lassos. Angabe relativ zur Position vom zugehörigen Objekt.
     * @param ropeLength Die Länge des Lassos. Dies ist ab sofort die maximale Länge, die die beiden Ankerpunkte
     *                   der Objekte voneinader entfernt sein können.
     *
     * @return Ein <code>Joint</code>-Objekt, mit dem der Joint weiter gesteuert werden kann.
     *
     * @see org.jbox2d.dynamics.joints.RopeJoint
     */
    @API
    public Joint createRopeJoint(Actor other, Vector anchorA, Vector anchorB, float ropeLength) {
        return createJoint(() -> {
            assertSameWorld(other);

            RopeJointDef ropeJointDef = new RopeJointDef();
            ropeJointDef.bodyA = physicsHandler.getBody();
            ropeJointDef.bodyB = other.getPhysicsHandler().getBody();

            ropeJointDef.localAnchorA.set(anchorA.toVec2());
            ropeJointDef.localAnchorB.set(anchorB.toVec2());
            ropeJointDef.maxLength = ropeLength;

            return physicsHandler.getWorldHandler().getWorld().createJoint(ropeJointDef);
        });
    }

    /**
     * Erstellt einen Distance-Joint zwischen diesem und einem weiteren <code>Actor</code>-Objekt.
     *
     * @param other             Das zweite <code>Actor</code>-Objekt, das ab sofort mit dem zugehörigen
     *                          <code>Actor</code>-Objekt
     *                          über einen <code>DistanceJoint</code> verbunden sein soll.
     * @param anchorAAsWorldPos Der Ankerpunkt für das zugehörige <code>Actor</code>-Objekt. Der erste Befestigungspunkt
     *                          des Joints. Angabe als <b>Position auf der Zeichenebene</b>, also absolut.
     * @param anchorBAsWorldPos Der Ankerpunkt für das zweite <code>Actor</code>-Objekt, also <code>other</code>.
     *                          Der zweite Befestigungspunkt des Joints.
     *                          Angabe als <b>Position auf der Zeichenebene</b>, also absolut.
     *
     * @return Ein <code>Joint</code>-Objekt, mit dem der Joint weiter gesteuert werden kann.
     *
     * @see org.jbox2d.dynamics.joints.DistanceJoint
     */
    @API
    public Joint createDistanceJoint(Actor other, Vector anchorAAsWorldPos, Vector anchorBAsWorldPos) {
        return createJoint(() -> {
            assertSameWorld(other);

            DistanceJointDef distanceJointDef = new DistanceJointDef();
            distanceJointDef.initialize(physicsHandler.getBody(), other.getPhysicsHandler().getBody(), anchorAAsWorldPos.toVec2(), anchorBAsWorldPos.toVec2());

            return physicsHandler.getWorldHandler().getWorld().createJoint(distanceJointDef);
        });
    }

    private Joint createJoint(Supplier<org.jbox2d.dynamics.joints.Joint> jointSupplier) {
        CompletableFuture<org.jbox2d.dynamics.joints.Joint> jointFuture = new CompletableFuture<>();

        Game.afterWorldStep(() -> jointFuture.complete(jointSupplier.get()));

        return new Joint() {
            private CompletableFuture<org.jbox2d.dynamics.joints.Joint> future = jointFuture;

            @Override
            public void release() {
                if (future != null) {
                    future.thenAccept(joint -> Game.afterWorldStep(() -> org.jbox2d.dynamics.joints.Joint.destroy(joint)));
                    future = null;
                }
            }
        };
    }

    @API
    public float getTorque() {
        synchronized (physicsHandlerLock) {
            return physicsHandler.getTorque();
        }
    }

    @API
    public void setTorque(float value) {
        synchronized (physicsHandlerLock) {
            physicsHandler.setTorque(value);
        }
    }

    /**
     * Setzt die Position des <code>Actor</code>-Objektes gänzlich neu auf der Zeichenebene. Das Setzen ist technisch
     * gesehen eine Verschiebung von der aktuellen Position an die neue.
     *
     * @param x neue <code>getX</code>-Koordinate
     * @param y neue <code>getY</code>-Koordinate
     *
     * @see #setPosition(Vector)
     * @see #setCenter(float, float)
     * @see #setX(float)
     * @see #setY(float)
     */
    @API
    public void setPosition(float x, float y) {
        this.setPosition(new Vector(x, y));
    }

    /**
     * Setzt die Position des Objektes gänzlich neu auf der Zeichenebene. Das Setzen ist technisch
     * gesehen eine Verschiebung von der aktuellen Position an die neue.
     *
     * @param p Der neue Zielpunkt
     *
     * @see #setPosition(float, float)
     * @see #setCenter(float, float)
     * @see #setX(float)
     * @see #setY(float)
     */
    @API
    public void setPosition(Vector p) {
        this.move(new Vector(p.x - this.getX(), p.y - this.getY()));
    }

    /**
     * Verschiebt das Objekt ohne Bedingungen auf der Zeichenebene. Dies ist die <b>zentrale</b>
     * Methode zum
     *
     * @param v Der Vector, der die Verschiebung des Objekts angibt.
     *
     * @see Vector
     * @see #move(float, float)
     */
    @API
    public void move(Vector v) {
        synchronized (physicsHandlerLock) {
            physicsHandler.moveBy(v);
        }
    }

    /**
     * Verschiebt die Actor-Figur so, dass ihr Mittelpunkt die eingegebenen Koordinaten hat.
     * <p>
     * Diese Methode arbeitet vectorFromThisTo dem Mittelpunkt des das Objekt abdeckenden BoundingRechtecks
     * durch den Aufruf der Methode <code>center()</code>. Daher ist diese Methode in der Anwendung
     * auf ein ActorGroup-Objekt nicht unbedingt sinnvoll.
     *
     * @param x Die <code>getX</code>-Koordinate des neuen Mittelpunktes des Objektes
     * @param y Die <code>getY</code>-Koordinate des neuen Mittelpunktes des Objektes
     *
     * @see #setCenter(Vector)
     * @see #move(Vector)
     * @see #setPosition(float, float)
     * @see #setPosition(Vector)
     * @see #getCenter()
     */
    @API
    public void setCenter(float x, float y) {
        this.setCenter(new Vector(x, y));
    }

    /**
     * Verschiebt die Actor-Figur so, dass ihr Mittelpunkt die eingegebenen Koordinaten hat.<br>
     * Diese Methode Arbeitet vectorFromThisTo dem Mittelpunkt des das Objekt abdeckenden BoundingRechtecks
     * durch den Aufruf der Methode <code>center()</code>. Daher ist diese Methode im Anwand auf
     * ein ActorGroup-Objekt nicht unbedingt sinnvoll.<br> Macht dasselbe wie
     * <code>mittelPunktSetzen(p.getX, p.getY)</code>.
     *
     * @param p Der neue Mittelpunkt des Actor-Objekts
     *
     * @see #setCenter(float, float)
     * @see #move(Vector)
     * @see #setPosition(float, float)
     * @see #getCenter()
     */
    @API
    public void setCenter(Vector p) {
        this.move(this.getCenter().negate().add(p));
    }

    /**
     * Gibt die X-Koordinate der linken oberen Ecke zurück. Sollte das Raumobjekt nicht rechteckig
     * sein, so wird die Position der linken oberen Ecke des umschließenden Rechtecks genommen.
     * <p>
     *
     * @return <code>getX</code>-Koordinate
     *
     * @see #getY()
     * @see #getPosition()
     */
    @API
    public float getX() {
        return this.getPosition().x;
    }

    /**
     * Setzt die getX-Koordinate der Position des Objektes gänzlich neu auf der Zeichenebene. Das
     * Setzen ist technisch gesehen eine Verschiebung von der aktuellen Position an die neue.
     *
     * @param x neue <code>getX</code>-Koordinate
     *
     * @see #setPosition(float, float)
     * @see #setCenter(float, float)
     * @see #setY(float)
     */
    @API
    public void setX(float x) {
        this.move(x - getX(), 0);
    }

    /**
     * Gibt die getY-Koordinate der linken oberen Ecke zurück. Sollte das Raumobjekt nicht rechteckig
     * sein, so wird die Position der linken oberen Ecke des umschließenden Rechtecks genommen.
     *
     * @return <code>getY</code>-Koordinate
     *
     * @see #getX()
     * @see #getPosition()
     */
    @API
    public float getY() {
        return this.getPosition().y;
    }

    /**
     * Setzt die getY-Koordinate der Position des Objektes gänzlich neu auf der Zeichenebene. Das
     * Setzen ist technisch gesehen eine Verschiebung von der aktuellen Position an die neue. <br>
     * <br> <b>Achtung!</b><br> Bei <b>allen</b> Objekten ist die eingegebene Position die
     * linke, obere Ecke des Rechtecks, das die Figur optimal umfasst. Das heißt, dass dies bei
     * Kreisen z.B. <b>nicht</b> der Mittelpunkt ist! Hierfür gibt es die Sondermethode
     * <code>setCenter(int getX, int getY)</code>.
     *
     * @param y neue <code>getY</code>-Koordinate
     *
     * @see #setPosition(float, float)
     * @see #setCenter(float, float)
     * @see #setX(float)
     */
    @API
    public void setY(float y) {
        this.move(0, y - getY());
    }

    /**
     * Gibt den Mittelpunkt des Objektes in der Scene aus.
     *
     * @return Die Koordinaten des Mittelpunktes des Objektes
     *
     * @see #getPosition()
     */
    @API
    public Vector getCenter() {
        synchronized (physicsHandlerLock) {
            return physicsHandler.getCenter();
        }
    }

    /**
     * Verschiebt das Objekt.<br> Hierbei wird nichts anderes gemacht, als <code>move(new
     * Vector(getDX, getDY))</code> auszufuehren. Insofern ist diese Methode dafuer gut, sich nicht mit
     * der Klasse Vector auseinandersetzen zu muessen.
     *
     * @param dX Die Verschiebung in Richtung X
     * @param dY Die Verschiebung in Richtung Y
     *
     * @see #move(Vector)
     */
    @API
    public void move(float dX, float dY) {
        this.move(new Vector(dX, dY));
    }

    /**
     * Gibt die Position dieses Actor-Objekts aus.
     *
     * @return die aktuelle Position dieses <code>Actor</code>-Objekts.
     */
    @API
    public Vector getPosition() {
        synchronized (physicsHandlerLock) {
            return physicsHandler.getPosition();
        }
    }

    /**
     * Rotiert das Objekt.
     *
     * @param degree Der Winkel (in <b>Grad</b>), um den das Objekt rotiert werden soll.
     */
    @API
    public void rotateBy(float degree) {
        physicsHandler.rotateBy(degree);
    }

    /**
     * Gibt den Winkel aus, um den das Objekt derzeit rotiert ist.
     *
     * @return Der Winkel (in <b>Bogenmaß</b>), um den das Objekt derzeit rotiert ist. Jedes Objekt ist bei
     * Initialisierung nicht rotiert (<code>getRotation()</code> gibt direkt vectorFromThisTo Initialisierung
     * <code>0</code> zurück).
     */
    @API
    public float getRotation() {
        synchronized (physicsHandlerLock) {
            return physicsHandler.getRotation();
        }
    }

    /**
     * Setzt den Rotationswert des Objekts.
     *
     * @param degree Der Winkel (in <b>Grad</b>), um den das Objekt <b>von seiner Ausgangsposition bei
     *               Initialisierung</b> rotiert werden soll.
     */
    @API
    public void setRotation(float degree) {
        synchronized (physicsHandlerLock) {
            physicsHandler.rotateBy(degree - getRotation());
        }
    }
}