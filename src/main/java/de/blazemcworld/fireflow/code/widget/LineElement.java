package de.blazemcworld.fireflow.code.widget;

import com.google.gson.JsonObject;
import de.blazemcworld.fireflow.FireFlow;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.math.AffineTransformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class LineElement {

    public WidgetVec from;
    public WidgetVec to;
    private final DisplayEntity.TextDisplayEntity display;
    private boolean spawned = false;

    public LineElement(WidgetVec pos) {
        from = pos;
        to = pos;
        display = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, pos.world());

        display.setBackground(0);
        display.setLineWidth(Integer.MAX_VALUE);
        display.setInterpolationDuration(1);
        display.setTeleportDuration(1);
        display.setYaw(180);
        display.setText(Text.literal("-"));
    }

    public void update() {
        double dist = from.distance(to);

        float angle = (float) Math.atan2(to.y() - from.y(), from.x() - to.x());
        display.setPosition(new WidgetVec(
                from.editor(),
                (from.x() + to.x()) / 2 + (Math.cos(angle) * dist * 0.1 - Math.sin(angle) * 0.135),
                (from.y() + to.y()) / 2 + (-Math.sin(angle) * dist * 0.1 - Math.cos(angle) * 0.135)
        ).vec());

        display.setTransformation(new AffineTransformation(
                new Vector3f(),
                new Quaternionf(0, 0, Math.sin(angle * 0.5), (float) Math.cos(angle * 0.5)),
                new Vector3f((float) dist * 8, 1, 1),
                new Quaternionf()
        ));
        if (!spawned) {
            FireFlow.server.execute(() -> {
                if (display.isRemoved()) return;
                from.world().spawnEntity(display);
            });
            spawned = true;
        }

        sendWeb();
    }

    public void remove() {
        display.remove(Entity.RemovalReason.DISCARDED);

        JsonObject json = new JsonObject();
        json.addProperty("type", "remove");
        json.addProperty("id", display.getUuid().toString());
        from.editor().webBroadcast(json);
    }

    public void color(TextColor color) {
        display.setText(Text.literal("-").setStyle(Style.EMPTY.withColor(color)));
        sendWeb();
    }

    private void sendWeb() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "line");
        json.addProperty("id", display.getUuid().toString());
        json.addProperty("fromX", from.x());
        json.addProperty("fromY", from.y());
        json.addProperty("toX", to.x());
        json.addProperty("toY", to.y());
        TextColor color = display.getText().getStyle().getColor();
        json.addProperty("color", color == null ? "" : color.getHexCode());
        from.editor().webBroadcast(json);
    }
}
