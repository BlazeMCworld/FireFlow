package de.blazemcworld.fireflow.code.type;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import de.blazemcworld.fireflow.FireFlow;
import net.minecraft.MinecraftVersion;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.registry.Registries;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Base64;

public class ItemType extends WireType<ItemStack> {

    public static final ItemType INSTANCE = new ItemType();
    private static final int currentDataFixer = MinecraftVersion.CURRENT.getSaveVersion().getId();

    private ItemType() {
        super("item", TextColor.fromFormatting(Formatting.GRAY), Items.ITEM_FRAME);
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
            NbtElement nbt = item.toNbt(FireFlow.server.getRegistryManager());
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            NbtIo.write(nbt, out);
            JsonObject json = new JsonObject();
            json.addProperty("data", new String(Base64.getEncoder().encode(out.toByteArray())));
            json.addProperty("version", currentDataFixer);
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
            NbtElement nbt = NbtIo.read(inp, NbtSizeTracker.of(1024 * 1024 * 2));
            int version = obj.get("version").getAsInt();
            if (version != currentDataFixer) {
                nbt = FireFlow.server.getDataFixer().update(TypeReferences.ITEM_STACK, new Dynamic<>(NbtOps.INSTANCE, nbt), version, currentDataFixer).getValue();
            }
            return ItemStack.fromNbt(FireFlow.server.getRegistryManager(), nbt).orElseGet(() -> new ItemStack(Items.AIR));
        } catch (Exception err) {
            FireFlow.LOGGER.error("Failed to deserialize item", err);
            return new ItemStack(Items.AIR);
        }
    }

    @Override
    public boolean valuesEqual(ItemStack a, ItemStack b) {
        return ItemStack.areEqual(a, b);
    }

    @Override
    public ItemStack parseInset(String str) {
        DataResult<Identifier> id = Identifier.validate(str);
        if (id.isError()) return new ItemStack(Items.AIR);
        return new ItemStack(Registries.ITEM.getOptionalValue(id.getOrThrow()).orElse(Items.AIR));
    }

    @Override
    public String getName() {
        return "Item";
    }

    @Override
    protected String stringifyInternal(ItemStack value) {
        return Registries.ITEM.getId(value.getItem()).getNamespace() + " x" + value.getCount();
    }

    @Override
    public boolean canConvert(WireType<?> other) {
        return other == StringType.INSTANCE;
    }

    @Override
    public ItemStack convert(WireType<?> other, Object v) {
        if (v instanceof String str) {
            DataResult<Identifier> id = Identifier.validate(str);
            if (id.isError()) return new ItemStack(Items.AIR);
            return new ItemStack(Registries.ITEM.getOptionalValue(id.getOrThrow()).orElse(Items.AIR));
        }
        return new ItemStack(Items.AIR);
    }
}
