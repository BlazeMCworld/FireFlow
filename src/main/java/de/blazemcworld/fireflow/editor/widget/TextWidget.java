package de.blazemcworld.fireflow.editor.widget;

import de.blazemcworld.fireflow.editor.Bounds;
import de.blazemcworld.fireflow.editor.Widget;
import de.blazemcworld.fireflow.util.TextWidth;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.instance.Instance;

public class TextWidget implements Widget {

    private final Instance inst;
    private final Entity display = new Entity(EntityType.TEXT_DISPLAY);
    private final TextDisplayMeta meta = (TextDisplayMeta) display.getEntityMeta();
    public Vec position;

    public TextWidget(Vec position, Instance inst, Component text) {
        this.inst = inst;
        this.position = position;
        meta.setText(text);
        meta.setBackgroundColor(0);
        meta.setLineWidth(Integer.MAX_VALUE);
        meta.setTransformationInterpolationDuration(1);
        meta.setPosRotInterpolationDuration(1);
        meta.setHasNoGravity(true);
        update();
    }

    @Override
    public Widget select(Player player, Vec cursor) {
        if (bounds().includes2d(cursor)) return this;
        return null;
    }

    @Override
    public void remove() {
        display.remove();
    }

    public void update() {
        Vec size = bounds().size();
        Vec adjusted = Vec.fromPoint(position).add(-size.x() * 0.5, 1 / 32.0, 0);
        display.setInstance(inst, adjusted.asPosition().withView(180, 0));
    }

    public Bounds bounds() {
        double width = TextWidth.calculate(meta.getText()) / 20;
        return new Bounds(position, Vec.fromPoint(position).add(-width * 0.5, 0.25, 0));
    }

    public void text(Component text) {
        meta.setText(text);
    }
}
