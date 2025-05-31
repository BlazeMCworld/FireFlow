package de.blazemcworld.fireflow.code.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.blazemcworld.fireflow.code.value.Position;
import net.minecraft.item.Items;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

public class PositionType extends WireType<Position> {

    public static final PositionType INSTANCE = new PositionType();

    private PositionType() {
        super("position", TextColor.fromFormatting(Formatting.DARK_PURPLE), Items.COMPASS);
    }

    @Override
    public Position defaultValue() {
        return new Position(Vec3d.ZERO, 0, 0);
    }

    @Override
    public Position checkType(Object obj) {
        if (obj instanceof Position p) return p;
        return null;
    }

    @Override
    public JsonElement toJson(Position pos) {
        JsonObject out = new JsonObject();
        out.addProperty("x", pos.xyz().x);
        out.addProperty("y", pos.xyz().y);
        out.addProperty("z", pos.xyz().z);
        out.addProperty("pitch", pos.pitch());
        out.addProperty("yaw", pos.yaw());
        return out;
    }

    @Override
    public Position fromJson(JsonElement json) {
        JsonObject obj = json.getAsJsonObject();
        return new Position(new Vec3d(
                obj.get("x").getAsDouble(),
                obj.get("y").getAsDouble(),
                obj.get("z").getAsDouble()
        ),
                obj.get("pitch").getAsFloat(),
                obj.get("yaw").getAsFloat()
        );
    }

    @Override
    public boolean valuesEqual(Position a, Position b) {
        return a.equals(b);
    }

    @Override
    protected String stringifyInternal(Position value, String mode) {
        return switch (mode) {
            case "x" -> "%.2f".formatted(value.xyz().x);
            case "y" -> "%.2f".formatted(value.xyz().y);
            case "z" -> "%.2f".formatted(value.xyz().z);
            case "pitch" -> "%.2f".formatted(value.pitch());
            case "yaw" -> "%.2f".formatted(value.yaw());
            default -> "(%.2f, %.2f, %.2f, %.2f, %.2f)".formatted(
                    value.xyz().x,
                    value.xyz().y,
                    value.xyz().z,
                    value.pitch(),
                    value.yaw()
            );
        };
    }

    @Override
    public String getName() {
        return "Position";
    }

    @Override
    protected boolean canConvertInternal(WireType<?> other) {
        return other == VectorType.INSTANCE;
    }

    @Override
    protected Position convertInternal(WireType<?> other, Object v) {
        if (other == VectorType.INSTANCE && v instanceof Vec3d vec) {
            return new Position(vec, 0, 0);
        }
        return super.convertInternal(other, v);
    }
}
