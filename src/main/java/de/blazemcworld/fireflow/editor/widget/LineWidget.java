package de.blazemcworld.fireflow.editor.widget;

import de.blazemcworld.fireflow.editor.Widget;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.instance.Instance;

public class LineWidget implements Widget {

    private final Instance inst;
    private final Entity display = new Entity(EntityType.TEXT_DISPLAY);
    private final TextDisplayMeta meta = (TextDisplayMeta) display.getEntityMeta();
    public TextColor color = NamedTextColor.WHITE;
    public Vec from = Vec.ZERO;
    public Vec to = Vec.ZERO;

    public LineWidget(Instance inst) {
        this.inst = inst;
        meta.setText(Component.text("-").color(color));
        meta.setBackgroundColor(0);
        meta.setLineWidth(Integer.MAX_VALUE);
        meta.setTransformationInterpolationDuration(1);
        meta.setPosRotInterpolationDuration(1);
        meta.setHasNoGravity(true);
    }

    public void update() {
        double dist = from.distance(to);
        meta.setText(Component.text("-").color(color));
        meta.setScale(new Vec(dist * 8, 1, 1));
        float angle = (float) Math.atan2(to.y() - from.y(), from.x() - to.x());
        meta.setLeftRotation(new float[]{0, 0, (float) Math.sin(angle * 0.5), (float) Math.cos(angle * 0.5)});
        Vec v = Vec.fromPoint(from).add(to).mul(0.5).add(
                Math.cos(angle) * dist * 0.1 - Math.sin(angle) * 0.1625,
                -Math.sin(angle) * dist * 0.1 - Math.cos(angle) * 0.1625,
                0
        );
        display.setInstance(inst, v.withZ(15.999).asPosition().withView(180f, 0f));
    }

    public double distance(Vec pos) {
        if (from == to) return from.distance(pos);
        Vec ab = Vec.fromPoint(to).sub(from);
        Vec ap = Vec.fromPoint(pos).sub(from);

        double projection = (ap.x() * ab.x() + ap.y() * ab.y()) / (ab.x() * ab.x() + ab.y() * ab.y());

        Vec closestPoint = projection <= 0 ? from : projection >= 1 ? to :
                new Vec(from.x() + projection * ab.x(), from.y() + projection * ab.y(), 0);

        return closestPoint.withZ(pos.z()).distance(pos);
    }

    @Override
    public void remove() {
        display.remove();
    }
}
