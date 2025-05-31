package de.blazemcworld.fireflow.code.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.item.Items;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

public class NumberType extends WireType<Double> {

    public static final NumberType INSTANCE = new NumberType();

    private NumberType() {
        super("number", TextColor.fromFormatting(Formatting.GREEN), Items.CLOCK);
    }

    @Override
    public Double defaultValue() {
        return 0.0;
    }

    @Override
    public String getName() {
        return "Number";
    }

    @Override
    public Double parseInset(String str) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    protected String stringifyInternal(Double value, String mode) {
        return switch (mode) {
            case "hex", "hexadecimal" -> String.format("%02x", value.intValue());
            case "dec", "decimal", "int", "integer" -> String.valueOf(value.intValue());
            default -> String.valueOf(value);
        };
    }

    @Override
    public Double checkType(Object obj) {
        if (obj instanceof Double d) return d;
        return null;
    }

    @Override
    public JsonElement toJson(Double obj) {
        return new JsonPrimitive(obj);
    }

    @Override
    public Double fromJson(JsonElement json) {
        return json.getAsDouble();
    }

    @Override
    public boolean valuesEqual(Double a, Double b) {
        return a.equals(b);
    }
}
