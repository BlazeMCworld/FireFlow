package de.blazemcworld.fireflow.code.widget;

import com.google.gson.JsonObject;
import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.code.CodeInteraction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.Direction;
import org.joml.Vector3f;

import java.util.List;

public class ItemWidget extends Widget {

    private final DisplayEntity.ItemDisplayEntity display;
    private boolean spawned = false;
    private final double size;

    public ItemWidget(WidgetVec pos, ItemStack item, double size) {
        super(pos);
        display = new DisplayEntity.ItemDisplayEntity(EntityType.ITEM_DISPLAY, pos.world());
        display.setItemStack(item);
        display.setInterpolationDuration(1);
        display.setTeleportDuration(1);
        display.setYaw(180);
        this.size = size;
    }

    public ItemWidget(WidgetVec pos, Item type) {
        this(pos, new ItemStack(type), 0.25);
    }

    @Override
    public WidgetVec size() {
        return new WidgetVec(pos().editor(), size, size);
    }

    @Override
    public void update() {
        display.setPosition(pos().vec().withAxis(Direction.Axis.Z, 15.99));
        display.setTeleportDuration(1);
        display.setInterpolationDuration(1);
        display.setItemDisplayContext(ItemDisplayContext.GUI);
        display.setTransformation(new AffineTransformation(
                new Vector3f((float) size / 2, (float) -size / 2, 0),
                DisplayEntity.getTransformation(display.getDataTracker()).getLeftRotation(),
                new Vector3f((float) -size, (float) size, -0.001f),
                DisplayEntity.getTransformation(display.getDataTracker()).getRightRotation()
        ));
        if (!spawned) {
            FireFlow.server.execute(() -> pos().world().spawnEntity(display));
            spawned = true;
        }

        JsonObject json = new JsonObject();
        json.addProperty("type", "item");
        json.addProperty("id", display.getUuid().toString());
        json.addProperty("x", pos().x());
        json.addProperty("y", pos().y());
        json.addProperty("size", size);
        json.addProperty("item", Registries.ITEM.getId(display.getItemStack().getItem()).getPath());
        pos().editor().webBroadcast(json);
    }

    @Override
    public List<Widget> getChildren() {
        return List.of();
    }

    @Override
    public void remove() {
        display.remove(Entity.RemovalReason.DISCARDED);

        JsonObject json = new JsonObject();
        json.addProperty("type", "remove");
        json.addProperty("id", display.getUuid().toString());
        pos().editor().webBroadcast(json);
    }

    @Override
    public boolean interact(CodeInteraction i) {
        return false;
    }
}
