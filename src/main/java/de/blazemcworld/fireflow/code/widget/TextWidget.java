package de.blazemcworld.fireflow.code.widget;

import de.blazemcworld.fireflow.code.Interaction;
import de.blazemcworld.fireflow.util.TextWidth;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.instance.InstanceContainer;

import java.util.List;

public class TextWidget implements Widget {

    private final Entity display = new Entity(EntityType.TEXT_DISPLAY);
    private final TextDisplayMeta meta = (TextDisplayMeta) display.getEntityMeta();
    private Vec pos = Vec.ZERO;
    public boolean shiftRight = false;
    private Vec scale = Vec.ONE;

    public TextWidget(Component text) {
        meta.setText(text);
        meta.setBackgroundColor(0);
        meta.setLineWidth(Integer.MAX_VALUE);
        meta.setTransformationInterpolationDuration(1);
        meta.setPosRotInterpolationDuration(1);
        meta.setHasNoGravity(true);
    }

    @Override
    public void setPos(Vec pos) {
        this.pos = pos;
    }

    @Override
    public Vec getPos() {
        return pos;
    }

    public void update(InstanceContainer inst) {
        double width = TextWidth.calculate(meta.getText()) / 40 * scale.x();
        Vec adjusted = Vec.fromPoint(pos).add(-width / 2, (-1 / 32.0 - 0.23) * scale.y(), 0);
        if (shiftRight) adjusted = adjusted.add(width - Math.ceil(width * 8) / 8, 0, 0);
        display.setInstance(inst, adjusted.asPosition().withView(180, 0));
    }

    @Override
    public Vec getSize() {
        return new Vec(Math.ceil(TextWidth.calculate(meta.getText()) / 40 * scale.x() * 8) / 8, 0.25 * scale.y(), 0);
    }

    public Vec getRawSize() {
        return new Vec(TextWidth.calculate(meta.getText()) / 40 * scale.x(), 0.25 * scale.y(), 0);
    }

    public Component text() {
        return meta.getText();
    }

    public void text(Component text) {
        meta.setText(text);
    }

    @Override
    public void remove() {
        display.remove();
    }

    @Override
    public boolean interact(Interaction i) {
        return false;
    }

    @Override
    public List<Widget> getChildren() {
        return null;
    }

    public void setRotation(double rotation) {
        rotation = Math.toRadians(rotation);
        meta.setLeftRotation(new float[]{0, 0, (float) Math.sin(rotation * 0.5), (float) Math.cos(rotation * 0.5)});
    }

    public TextWidget stretch(double x, double y) {
        scale = new Vec(x, y, 1);
        meta.setScale(scale);
        return this;
    }
}
