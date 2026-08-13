package de.blazemcworld.fireflow.code.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.blazemcworld.fireflow.code.value.Position;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class VectorType extends WireType<Vec3> {

    public static final VectorType INSTANCE = new VectorType();

    private VectorType() {
        super("vector", TextColor.RED, Items.ARROW);
    }

    @Override
    public Vec3 defaultValue() {
        return Vec3.ZERO;
    }

    @Override
    public Vec3 checkType(Object obj) {
        if (obj instanceof Vec3 p) return p;
        return null;
    }

    @Override
    public JsonElement toJson(Vec3 vec) {
        JsonObject out = new JsonObject();
        out.addProperty("x", vec.x);
        out.addProperty("y", vec.y);
        out.addProperty("z", vec.z);
        return out;
    }

    @Override
    public Vec3 fromJson(JsonElement json) {
        JsonObject obj = json.getAsJsonObject();
        return new Vec3(
                obj.get("x").getAsDouble(),
                obj.get("y").getAsDouble(),
                obj.get("z").getAsDouble()
        );
    }

    @Override
    public String getName() {
        return "Vector";
    }

    @Override
    public boolean valuesEqual(Vec3 a, Vec3 b) {
        return a.equals(b);
    }

    @Override
    protected String stringifyInternal(Vec3 value, String mode) {
        return switch (mode) {
            case "x" -> "%.2f".formatted(value.x);
            case "y" -> "%.2f".formatted(value.y);
            case "z" -> "%.2f".formatted(value.z);
            default -> "<%.2f, %.2f, %.2f>".formatted(
                    value.x,
                    value.y,
                    value.z
            );
        };
    }

    @Override
    protected boolean canConvertInternal(WireType<?> other) {
        return other == PositionType.INSTANCE;
    }

    @Override
    protected Vec3 convertInternal(WireType<?> other, Object v) {
        if (other == PositionType.INSTANCE && v instanceof Position pos) {
            return pos.xyz();
        }
        return super.convertInternal(other, v);
    }
}
