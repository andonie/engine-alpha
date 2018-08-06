package ea.animation;

import ea.Vector;
import ea.actor.Actor;
import ea.animation.interpolation.LinearFloat;

/**
 * Eine Animation, die ein Actor-Objekt in einer Linie animiert.
 */
public class LineAnimation
extends ActorAnimation {

    /**
     * Erstellt eine neue Linien-Animation.
     * @param actor         Der Actor, der zwischen seinem aktuellen Mittelpunkt und einem Endpunkt bewegt werden soll.
     * @param endPoint      Der Endpunkt. Die Bewegung des Aktors endet mit seinem Mittelpunkt auf dem
     *                      <code>endPoint</code>.
     * @param durationInMS  Die Zeit in Millisekunden, in der der Actor von seiner Ausgangsposition bis zum
     *                      Zielpunkt benötigt.
     * @param pingpong      <code>false</code>: Die Animation endet, wenn der Actor den Zielpunkt erreicht hat.
     *                      <code>true</code>: Der Actor bewegt sich zwischen seinem Ausgangspunkt und dem Zielpunkt
     *                      hin und her. Jede Strecke in eine Richtung dauert <code>durationInMS</code>. Die Animation
     *                      endet nicht von sich aus.
     */
    public LineAnimation(Actor actor, Vector endPoint, int durationInMS, boolean pingpong) {
        super(actor);

        Vector center = actor.position.getCenter();
        ValueAnimator<Float> aX = new ValueAnimator<>(durationInMS,
                x->actor.position.setCenter(x, actor.position.getCenter().y),
                new LinearFloat(center.x, endPoint.x),
                pingpong ? ValueAnimator.Mode.PINGPONG : ValueAnimator.Mode.SINGLE);
        ValueAnimator<Float> aY = new ValueAnimator<>(durationInMS,
                y->actor.position.setCenter(actor.position.getCenter().x, y),
                new LinearFloat(center.y, endPoint.y),
                pingpong ? ValueAnimator.Mode.PINGPONG : ValueAnimator.Mode.SINGLE);
        addAnimator(aX);
        addAnimator(aY);
    }
}