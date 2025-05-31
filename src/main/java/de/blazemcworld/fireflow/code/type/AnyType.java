package de.blazemcworld.fireflow.code.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.blazemcworld.fireflow.code.value.AnyValue;
import net.minecraft.item.Items;
import net.minecraft.text.TextColor;

public class AnyType extends WireType<AnyValue<?>> {
    public static final AnyType INSTANCE = new AnyType();

    private AnyType() {
        super("any", TextColor.fromRgb(0x91382E), Items.EXPERIENCE_BOTTLE);
    }

    @Override
    public AnyValue<?> defaultValue() {
        return new AnyValue<>(0.0, NumberType.INSTANCE);
    }

    @Override
    public AnyValue<?> checkType(Object obj) {
        if (obj instanceof AnyValue<?> a) return a;
        return null;
    }

    @Override
    public JsonElement toJson(AnyValue<?> obj) {
        JsonObject out = new JsonObject();
        out.add("type", AllTypes.toJson(obj.type()));
        out.add("value", obj.type().convertToJson(obj.value()));
        return out;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AnyValue<?> fromJson(JsonElement json) {
        JsonObject obj = json.getAsJsonObject();
        WireType<?> type = AllTypes.fromJson(obj.get("type"));
        if (type == null) return defaultValue();
        return new AnyValue<>(type.fromJson(obj.get("value")), (WireType<Object>) type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean valuesEqual(AnyValue<?> a, AnyValue<?> b) {
        if (a.type() != b.type()) return false;
        AnyValue<Object> genericsA = (AnyValue<Object>) a;
        AnyValue<Object> genericsB = (AnyValue<Object>) b;
        return genericsA.type().valuesEqual(genericsA.value(), genericsB.value());
    }

    @Override
    protected String stringifyInternal(AnyValue<?> value, String mode) {
        return value.type().stringify(value.value(), mode);
    }

    @Override
    protected boolean canConvertInternal(WireType<?> other) {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected AnyValue<?> convertInternal(WireType<?> other, Object v) {
        if (other == AnyType.INSTANCE) return (AnyValue<?>) v;
        return new AnyValue<>(v, (WireType<Object>) other);
    }

    @Override
    @SuppressWarnings("unchecked")
    public AnyValue<?> parseInset(String str) {
        if (!str.contains(":")) return null;
        int splitIndex = str.indexOf(':');
        String typeId = str.substring(0, splitIndex);
        String value = str.substring(splitIndex + 1);
        for (WireType<?> t : AllTypes.all) {
            if (t == AnyType.INSTANCE || !AllTypes.isValue(t)) continue;
            if (!t.id.equals(typeId)) continue;
            Object out = t.parseInset(value);
            if (out != null) return new AnyValue<>(out, (WireType<Object>) t);
        }
        return null;
    }

    @Override
    public String getName() {
        return "Any";
    }
}
