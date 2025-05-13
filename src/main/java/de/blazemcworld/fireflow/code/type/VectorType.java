package de.blazemcworld.fireflow.code.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.blazemcworld.fireflow.code.value.Position;
import net.minecraft.item.Items;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

public class VectorType extends WireType<Vec3d> {

    public static final VectorType INSTANCE = new VectorType();

    private VectorType() {
        super("vector", TextColor.fromFormatting(Formatting.RED), Items.ARROW);
    }

    @Override
    public Vec3d defaultValue() {
        return Vec3d.ZERO;
    }

    @Override
    public Vec3d checkType(Object obj) {
        if (obj instanceof Vec3d p) return p;
        return null;
    }

    @Override
    public JsonElement toJson(Vec3d vec) {
        JsonObject out = new JsonObject();
        out.addProperty("x", vec.x);
        out.addProperty("y", vec.y);
        out.addProperty("z", vec.z);
        return out;
    }

    @Override
    public Vec3d fromJson(JsonElement json) {
        JsonObject obj = json.getAsJsonObject();
        return new Vec3d(
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
    public boolean valuesEqual(Vec3d a, Vec3d b) {
        return a.equals(b);
    }

    @Override
    protected String stringifyInternal(Vec3d value) {
        return "<%.2f, %.2f, %.2f>".formatted(
                value.x,
                value.y,
                value.z
        );
    }

    @Override
    protected boolean canConvertInternal(WireType<?> other) {
        return other == PositionType.INSTANCE;
    }

    @Override
    protected Vec3d convertInternal(WireType<?> other, Object v) {
        if (other == PositionType.INSTANCE && v instanceof Position pos) {
            return pos.xyz();
        }
        return super.convertInternal(other, v);
    }
}
