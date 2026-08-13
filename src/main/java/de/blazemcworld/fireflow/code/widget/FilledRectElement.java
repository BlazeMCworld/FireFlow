package de.blazemcworld.fireflow.code.widget;

import com.google.gson.JsonObject;
import com.mojang.math.Transformation;
import de.blazemcworld.fireflow.FireFlow;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import org.joml.Vector3f;

public class FilledRectElement {
    
    public WidgetVec pos;
    public WidgetVec size;
    public int color;
    private boolean spawned = false;

    private final Display.TextDisplay display;

    public FilledRectElement(WidgetVec pos, int color) {
        this.pos = pos;
        this.color = color;
        display = new Display.TextDisplay(EntityTypes.TEXT_DISPLAY, pos.level());
        display.setBackgroundColor(color);
        display.setText(Component.literal(" "));
        display.setLineWidth(Integer.MAX_VALUE);
        display.setTransformationInterpolationDuration(1);
        display.setPosRotInterpolationDuration(1);
        display.setYRot(180);
    }

    public void update() {
        Transformation transform = Display.createTransformation(display.getEntityData());
        display.setTransformation(new Transformation(
                transform.translation(),
                transform.leftRotation(),
                new Vector3f((float) size.x() * 8, (float) size.y() * 4, 1),
                transform.rightRotation()
        ));
        display.setPos(pos.sub(size.x() / 2.5, size.y()).vec().with(Direction.Axis.Z, 15.9995));
        display.setBackgroundColor(color);

        if (!spawned) {
            FireFlow.server.execute(() -> {
                if (display.isRemoved()) return;
                pos.level().addFreshEntity(display);
            });
            spawned = true;
        }

        JsonObject json = new JsonObject();
        json.addProperty("type", "filled-rect");
        json.addProperty("id", display.getUUID().toString());
        json.addProperty("x", pos.x());
        json.addProperty("y", pos.y());
        json.addProperty("width", size.x());
        json.addProperty("height", size.y());
        json.addProperty("color", TextColor.fromRgb(color).formatValue());
        pos.editor().webBroadcast(json);
    }

    public void remove() {
        display.remove(Entity.RemovalReason.DISCARDED);

        JsonObject json = new JsonObject();
        json.addProperty("type", "remove");
        json.addProperty("id", display.getUUID().toString());
        pos.editor().webBroadcast(json);
    }
}