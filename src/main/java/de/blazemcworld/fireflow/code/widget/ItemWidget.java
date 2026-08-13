package de.blazemcworld.fireflow.code.widget;

import com.google.gson.JsonObject;
import com.mojang.math.Transformation;
import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.code.CodeInteraction;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

import java.util.List;

public class ItemWidget extends Widget {

    private final Display.ItemDisplay display;
    private boolean spawned = false;
    private final double size;

    public ItemWidget(WidgetVec pos, ItemStack item, double size) {
        super(pos);
        display = new Display.ItemDisplay(EntityTypes.ITEM_DISPLAY, pos.level());
        display.setItemStack(item);
        display.setPosRotInterpolationDuration(1);
        display.setTransformationInterpolationDuration(1);
        display.setYRot(180);
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
        display.setPos(pos().vec().with(Direction.Axis.Z, 15.99));
        display.setItemTransform(ItemDisplayContext.GUI);
        display.setTransformation(new Transformation(
                new Vector3f((float) size / 2, (float) -size / 2, 0),
                Display.createTransformation(display.getEntityData()).leftRotation(),
                new Vector3f((float) -size, (float) size, -0.001f),
                Display.createTransformation(display.getEntityData()).rightRotation()
        ));
        if (!spawned) {
            FireFlow.server.execute(() -> {
                if (display.isRemoved()) return;
                pos().level().addFreshEntity(display);
            });
            spawned = true;
        }

        JsonObject json = new JsonObject();
        json.addProperty("type", "item");
        json.addProperty("id", display.getUUID().toString());
        json.addProperty("x", pos().x());
        json.addProperty("y", pos().y());
        json.addProperty("size", size);
        json.addProperty("item", BuiltInRegistries.ITEM.getKey(display.getItemStack().getItem()).getPath());
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
        json.addProperty("id", display.getUUID().toString());
        pos().editor().webBroadcast(json);
    }

    @Override
    public boolean interact(CodeInteraction i) {
        return false;
    }
}
