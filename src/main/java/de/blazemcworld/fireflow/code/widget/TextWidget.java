package de.blazemcworld.fireflow.code.widget;

import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.code.CodeInteraction;
import de.blazemcworld.fireflow.util.TextWidth;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.AffineTransformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class TextWidget extends Widget {

    private final DisplayEntity.TextDisplayEntity display;
    private boolean spawned = false;
    private double xScale = 1;
    private double yScale = 1;

    public TextWidget(WidgetVec pos) {
        super(pos);
        display = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, pos.world());
        display.setBackground(0);
        display.setLineWidth(Integer.MAX_VALUE);
        display.setInterpolationDuration(1);
        display.setTeleportDuration(1);
        display.setYaw(180);
    }

    public TextWidget(WidgetVec pos, Text text) {
        this(pos);
        setText(text);
    }

    public void setText(Text text) {
        display.setText(text);
    }

    @Override
    public void update() {
        WidgetVec pos = pos();
        WidgetVec size = size();

        pos = pos.add(-size.x() / 2.0, -size.y());

        display.setPosition(pos.vec());
        if (!spawned) {
            FireFlow.server.execute(() -> pos().world().spawnEntity(display));
            spawned = true;
        }
    }

    @Override
    public void remove() {
        if (spawned) display.remove(Entity.RemovalReason.DISCARDED);
    }

    @Override
    public List<Widget> getChildren() {
        return List.of();
    }

    @Override
    public WidgetVec size() {
        return new WidgetVec(pos().editor(), TextWidth.calculate(display.getText()) / 40.0 * xScale, 0.25 * yScale);
    }

    public TextWidget stretch(double x, double y) {
        xScale = x;
        yScale = y;
        AffineTransformation transform = DisplayEntity.getTransformation(display.getDataTracker());
        display.setTransformation(new AffineTransformation(
                transform.getTranslation(),
                transform.getLeftRotation(),
                new Vector3f((float) xScale, (float) yScale, 1),
                transform.getRightRotation()
        ));
        return this;
    }

    public void setRotation(double rotation) {
        rotation = Math.toRadians(rotation);
        AffineTransformation transform = DisplayEntity.getTransformation(display.getDataTracker());
        display.setTransformation(new AffineTransformation(
                transform.getTranslation(),
                new Quaternionf(0, 0, (float) Math.sin(rotation * 0.5), (float) Math.cos(rotation * 0.5)),
                transform.getScale(),
                transform.getRightRotation()
        ));
    }

    @Override
    public boolean interact(CodeInteraction i) {
        return false;
    }
}
