package de.blazemcworld.fireflow.code.widget;

import com.google.gson.JsonObject;
import com.mojang.math.Transformation;
import de.blazemcworld.fireflow.FireFlow;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class LineElement {

    public WidgetVec from;
    public WidgetVec to;
    private final Display.TextDisplay display;
    private boolean spawned = false;

    public LineElement(WidgetVec pos) {
        from = pos;
        to = pos;
        display = new Display.TextDisplay(EntityTypes.TEXT_DISPLAY, pos.level());

        display.setBackgroundColor(0);
        display.setLineWidth(Integer.MAX_VALUE);
        display.setPosRotInterpolationDuration(1);
        display.setTransformationInterpolationDuration(1);
        display.setYRot(180);
        display.setText(Component.literal("-"));
    }

    public void update() {
        double dist = from.distance(to);

        float angle = (float) Math.atan2(to.y() - from.y(), from.x() - to.x());
        display.setPos(new WidgetVec(
                from.editor(),
                (from.x() + to.x()) / 2 + (Math.cos(angle) * dist * 0.1 - Math.sin(angle) * 0.135),
                (from.y() + to.y()) / 2 + (-Math.sin(angle) * dist * 0.1 - Math.cos(angle) * 0.135)
        ).vec());

        display.setTransformation(new Transformation(
                new Vector3f(),
                new Quaternionf(0, 0, Math.sin(angle * 0.5), (float) Math.cos(angle * 0.5)),
                new Vector3f((float) dist * 8, 1, 1),
                new Quaternionf()
        ));
        if (!spawned) {
            FireFlow.server.execute(() -> {
                if (display.isRemoved()) return;
                from.level().addFreshEntity(display);
            });
            spawned = true;
        }

        sendWeb();
    }

    public void remove() {
        display.remove(Entity.RemovalReason.DISCARDED);

        JsonObject json = new JsonObject();
        json.addProperty("type", "remove");
        json.addProperty("id", display.getUUID().toString());
        from.editor().webBroadcast(json);
    }

    public void color(TextColor color) {
        display.setText(Component.literal("-").setStyle(Style.EMPTY.withColor(color)));
        sendWeb();
    }

    private void sendWeb() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "line");
        json.addProperty("id", display.getUUID().toString());
        json.addProperty("fromX", from.x());
        json.addProperty("fromY", from.y());
        json.addProperty("toX", to.x());
        json.addProperty("toY", to.y());
        TextColor color = display.getText().getStyle().getColor();
        json.addProperty("color", color == null ? "" : color.formatValue());
        from.editor().webBroadcast(json);
    }
}
