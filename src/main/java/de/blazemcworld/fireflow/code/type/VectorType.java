package de.blazemcworld.fireflow.code.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.item.Material;

public class VectorType extends WireType<Vec> {

    public static final VectorType INSTANCE = new VectorType();

    private VectorType() {
        super("vector", NamedTextColor.RED, Material.ARROW);
    }

    @Override
    public Vec defaultValue() {
        return Vec.ZERO;
    }

    @Override
    public Vec checkType(Object obj) {
        if (obj instanceof Vec p) return p;
        return null;
    }

    @Override
    public JsonElement toJson(Vec vec) {
        JsonObject out = new JsonObject();
        out.addProperty("x", vec.x());
        out.addProperty("y", vec.y());
        out.addProperty("z", vec.z());
        return out;
    }

    @Override
    public Vec fromJson(JsonElement json) {
        JsonObject obj = json.getAsJsonObject();
        return new Vec(
                obj.get("x").getAsDouble(),
                obj.get("y").getAsDouble(),
                obj.get("z").getAsDouble()
        );
    }

    @Override
    public boolean valuesEqual(Vec a, Vec b) {
        return a.equals(b);
    }

    @Override
    protected String stringifyInternal(Vec value) {
        return "<%.2f, %.2f, %.2f>".formatted(
                value.x(),
                value.y(),
                value.z()
        );
    }

    @Override
    protected boolean canConvertInternal(WireType<?> other) {
        return other == PositionType.INSTANCE;
    }

    @Override
    protected Vec convertInternal(WireType<?> other, Object v) {
        if (other == PositionType.INSTANCE && v instanceof Pos pos) {
            return pos.asVec();
        }
        return super.convertInternal(other, v);
    }
}
