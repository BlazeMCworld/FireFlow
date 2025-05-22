package de.blazemcworld.fireflow.code.widget;

import com.google.gson.JsonObject;
import de.blazemcworld.fireflow.FireFlow;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.Direction;
import org.joml.Vector3f;

public class FilledRectElement {
    
    public WidgetVec pos;
    public WidgetVec size;
    public int color;
    private boolean spawned = false;

    private final DisplayEntity.TextDisplayEntity display;

    public FilledRectElement(WidgetVec pos, int color) {
        this.pos = pos;
        this.color = color;
        display = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, pos.world());
        display.setBackground(color);
        display.setText(Text.literal(" "));
        display.setLineWidth(Integer.MAX_VALUE);
        display.setInterpolationDuration(1);
        display.setTeleportDuration(1);
        display.setYaw(180);
    }

    public void update() {
        AffineTransformation transform = DisplayEntity.getTransformation(display.getDataTracker());
        display.setTransformation(new AffineTransformation(
                transform.getTranslation(),
                transform.getLeftRotation(),
                new Vector3f((float) size.x() * 8, (float) size.y() * 4, 1),
                transform.getRightRotation()
        ));
        display.setPosition(pos.sub(size.x() / 2.5, size.y()).vec().withAxis(Direction.Axis.Z, 15.9995));
        display.setBackground(color);

        if (!spawned) {
            FireFlow.server.execute(() -> pos.world().spawnEntity(display));
            spawned = true;
        }

        JsonObject json = new JsonObject();
        json.addProperty("type", "filled-rect");
        json.addProperty("id", display.getUuid().toString());
        json.addProperty("x", pos.x());
        json.addProperty("y", pos.y());
        json.addProperty("width", size.x());
        json.addProperty("height", size.y());
        json.addProperty("color", TextColor.fromRgb(color).getHexCode());
        pos.editor().webBroadcast(json);
    }

    public void remove() {
        display.remove(Entity.RemovalReason.DISCARDED);

        JsonObject json = new JsonObject();
        json.addProperty("type", "remove");
        json.addProperty("id", display.getUuid().toString());
        pos.editor().webBroadcast(json);
    }
}