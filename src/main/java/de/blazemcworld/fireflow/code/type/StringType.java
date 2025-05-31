package de.blazemcworld.fireflow.code.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.item.Items;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

public class StringType extends WireType<String> {

    public static final StringType INSTANCE = new StringType();

    private StringType() {
        super("string", TextColor.fromFormatting(Formatting.YELLOW), Items.STRING);
    }

    @Override
    public String getName() {
        return "String";
    }

    @Override
    public String defaultValue() {
        return "";
    }

    @Override
    public String parseInset(String str) {
        return str;
    }

    @Override
    protected String stringifyInternal(String value, String mode) {
        return value;
    }

    @Override
    public String checkType(Object obj) {
        if (obj instanceof String str) return str;
        return null;
    }

    @Override
    public JsonElement toJson(String obj) {
        return new JsonPrimitive(obj);
    }

    @Override
    public String fromJson(JsonElement json) {
        return json.getAsString();
    }

    @Override
    public boolean valuesEqual(String a, String b) {
        return a.equals(b);
    }

    @Override
    protected boolean canConvertInternal(WireType<?> other) {
        return AllTypes.isValue(other);
    }

    @Override
    protected String convertInternal(WireType<?> other, Object v) {
        return other.stringify(v, "display");
    }
}
