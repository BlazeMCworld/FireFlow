package de.blazemcworld.fireflow.code.widget;

import com.google.gson.JsonObject;
import com.mojang.math.Transformation;
import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.code.CodeInteraction;
import de.blazemcworld.fireflow.util.TextWidth;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class TextWidget extends Widget {

    private final Display.TextDisplay display;
    private boolean spawned = false;
    private double xScale = 1;
    private double yScale = 1;
    private int rotation = 0;

    public TextWidget(WidgetVec pos) {
        super(pos);
        display = new Display.TextDisplay(EntityTypes.TEXT_DISPLAY, pos.level());
        display.setBackgroundColor(0);
        display.setLineWidth(Integer.MAX_VALUE);
        display.setTransformationInterpolationDuration(1);
        display.setPosRotInterpolationDuration(1);
        display.setYRot(180);
    }

    public TextWidget(WidgetVec pos, Component text) {
        this(pos);
        setText(text);
    }

    public void setText(Component text) {
        display.setText(text);
    }

    @Override
    public void update() {
        WidgetVec pos = pos();
        WidgetVec size = size();

        pos = pos.add(-size.x() / 2.0, -size.y());

        display.setPos(pos.vec());
        if (!spawned) {
            FireFlow.server.execute(() -> {
                if (display.isRemoved()) return;
                pos().level().addFreshEntity(display);
            });
            spawned = true;
        }

        JsonObject json = new JsonObject();
        json.addProperty("type", "text");
        json.addProperty("id", display.getUUID().toString());
        json.addProperty("x", pos().x());
        json.addProperty("y", pos().y());
        json.addProperty("text", getPlainText(display.getText()));
        json.addProperty("scaleX", xScale);
        json.addProperty("scaleY", yScale);
        json.addProperty("rotation", rotation);
        TextColor c = display.getText().getStyle().getColor();
        json.addProperty("color", c == null ? "" : c.formatValue());
        pos.editor().webBroadcast(json);
    }

    private String getPlainText(Component text) {
        StringBuilder builder = new StringBuilder();
        if (text.getContents() instanceof PlainTextContents.LiteralContents(String literal)) {
            builder.append(literal);
        }
        for (Component child : text.getSiblings()) {
            builder.append(getPlainText(child));
        }
        return builder.toString();
    }

    @Override
    public void remove() {
        if (spawned) display.remove(Entity.RemovalReason.DISCARDED);

        JsonObject json = new JsonObject();
        json.addProperty("type", "remove");
        json.addProperty("id", display.getUUID().toString());
        pos().editor().webBroadcast(json);
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
        Transformation transform = Display.createTransformation(display.getEntityData());
        display.setTransformation(new Transformation(
                transform.translation(),
                transform.leftRotation(),
                new Vector3f((float) xScale, (float) yScale, 1),
                transform.rightRotation()
        ));
        return this;
    }

    public void setRotation(int deg) {
        this.rotation = deg;
        double rotation = Math.toRadians(deg);
        Transformation transform = Display.createTransformation(display.getEntityData());
        display.setTransformation(new Transformation(
                transform.translation(),
                new Quaternionf(0, 0, (float) Math.sin(rotation * 0.5), (float) Math.cos(rotation * 0.5)),
                transform.scale(),
                transform.rightRotation()
        ));
    }

    @Override
    public boolean interact(CodeInteraction i) {
        return false;
    }
}
