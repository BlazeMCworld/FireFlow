package de.blazemcworld.fireflow.code.type;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import de.blazemcworld.fireflow.FireFlow;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Base64;
import java.util.Optional;

public class ItemType extends WireType<ItemStack> {

    public static final ItemType INSTANCE = new ItemType();

    private ItemType() {
        super("item", TextColor.GRAY, Items.ITEM_FRAME);
    }

    @Override
    public ItemStack defaultValue() {
        return new ItemStack(Items.AIR);
    }

    @Override
    public ItemStack checkType(Object obj) {
        if (obj instanceof ItemStack item) return item;
        return null;
    }

    @Override
    public JsonElement toJson(ItemStack item) {
        if (item.isEmpty()) return JsonNull.INSTANCE;
        try {
            CompoundTag tag = NbtUtils.addCurrentDataVersion(new CompoundTag());
            tag.put("item", ItemStack.CODEC.encodeStart(FireFlow.server.registryAccess().createSerializationContext(NbtOps.INSTANCE), item).getOrThrow());

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            NbtIo.write(tag, out);

            JsonObject json = new JsonObject();
            json.addProperty("data", new String(Base64.getEncoder().encode(out.toByteArray())));
            return json;
        } catch (Exception err) {
            FireFlow.LOGGER.error("Failed to serialize item", err);
            return JsonNull.INSTANCE;
        }
    }

    @Override
    public ItemStack fromJson(JsonElement json) {
        if (json.isJsonNull()) return new ItemStack(Items.AIR);
        try {
            JsonObject obj = json.getAsJsonObject();
            ByteArrayDataInput inp = ByteStreams.newDataInput(Base64.getDecoder().decode(obj.get("data").getAsString()));
            CompoundTag tag = NbtIo.read(inp, NbtAccounter.create(1024 * 1024 * 2));

            int version = NbtUtils.getDataVersion(tag);
            tag = DataFixTypes.HOTBAR.updateToCurrentVersion(FireFlow.server.getFixerUpper(), tag, version);

            return ItemStack.CODEC.parse(NbtOps.INSTANCE, tag.get("item")).getOrThrow();
        } catch (Exception err) {
            FireFlow.LOGGER.error("Failed to deserialize item", err);
            return new ItemStack(Items.AIR);
        }
    }

    @Override
    public boolean valuesEqual(ItemStack a, ItemStack b) {
        return ItemStack.isSameItemSameComponents(a, b);
    }

    @Override
    public ItemStack parseInset(String str) {
        DataResult<Identifier> id = Identifier.read(str);
        if (id.isError()) return null;
        Optional<Item> item = BuiltInRegistries.ITEM.getOptional(id.getOrThrow());
        return item.map(ItemStack::new).orElse(null);
    }

    @Override
    public String getName() {
        return "Item";
    }

    @Override
    protected String stringifyInternal(ItemStack value, String mode) {
        return switch (mode) {
            case "id", "type", "material" -> BuiltInRegistries.ITEM.getKey(value.getItem()).getPath();
            case "count" -> String.valueOf(value.getCount());
            default -> BuiltInRegistries.ITEM.getKey(value.getItem()).getPath() + " x" + value.getCount();
        };
    }

    @Override
    public boolean canConvert(WireType<?> other) {
        return other == StringType.INSTANCE;
    }

    @Override
    public ItemStack convert(WireType<?> other, Object v) {
        if (v instanceof String str) {
            DataResult<Identifier> id = Identifier.read(str);
            if (id.isError()) return new ItemStack(Items.AIR);
            return new ItemStack(BuiltInRegistries.ITEM.getOptional(id.getOrThrow()).orElse(Items.AIR));
        }
        return new ItemStack(Items.AIR);
    }
}
