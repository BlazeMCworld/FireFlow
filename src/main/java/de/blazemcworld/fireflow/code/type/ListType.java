package de.blazemcworld.fireflow.code.type;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import de.blazemcworld.fireflow.code.value.ListValue;
import net.minecraft.item.Items;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

public class ListType<T> extends WireType<ListValue<T>> {

    public static final ListType<?> UNSPECIFIED = new ListType<>(null);
    public final WireType<T> elementType;
    private static final WeakHashMap<WireType<?>, ListType<?>> instances = new WeakHashMap<>();

    private ListType(WireType<T> type) {
        super("list", computeColor(type), Items.BOOKSHELF);
        this.elementType = type;
    }

    @SuppressWarnings("unchecked")
    public static <T> ListType<T> of(WireType<T> type) {
        return (ListType<T>) instances.computeIfAbsent(type, ListType::new);
    }

    @Override
    public ListValue<T> defaultValue() {
        return new ListValue<>(elementType);
    }

    private static TextColor computeColor(WireType<?> type) {
        if (type == null) return TextColor.fromFormatting(Formatting.WHITE);
        int rgb = type.color.getRgb();
        int r = (rgb >> 16) & 0xFF / 2;
        int g = (rgb >> 8) & 0xFF / 2 + 127;
        int b = rgb & 0xFF / 2;
        return TextColor.fromRgb(r << 16 | g << 8 | b);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ListValue<T> checkType(Object obj) {
        if (obj instanceof ListValue<?> list && list.type == elementType) return (ListValue<T>) list;
        return null;
    }
    
    @Override
    public int getTypeCount() {
        return 1;
    }

    @Override
    public List<WireType<?>> getTypes() {
        return List.of(elementType);
    }

    @Override
    public WireType<?> withTypes(List<WireType<?>> types) {
        return ListType.of(types.get(0));
    }

    @Override
    public boolean acceptsType(WireType<?> type, int index) {
        return AllTypes.isValue(type);
    }

    @Override
    public String getName() {
        if (elementType == null) return "List";
        return "List<" + elementType.getName() + ">";
    }

    @Override
    protected String stringifyInternal(ListValue<T> value) {
        return value.type.getName() + " x" + value.size();
    }

    @Override
    public JsonElement toJson(ListValue<T> obj) {
        JsonArray array = new JsonArray();
        for (int i = 0; i < obj.size(); i++) {
            array.add(elementType.toJson(obj.get(i)));
        }
        return array;
    }

    @Override
    public ListValue<T> fromJson(JsonElement json) {
        List<T> values = new ArrayList<>();
        for (JsonElement elem : json.getAsJsonArray()) {
            values.add(elementType.fromJson(elem));
        }
        return new ListValue<>(elementType, values);
    }

    @Override
    public boolean valuesEqual(ListValue<T> a, ListValue<T> b) {
        if (a.size() != b.size()) return false;
        if (a.type != b.type) return false;
        for (int i = 0; i < a.size(); i++) {
            if (!elementType.valuesEqual(a.get(i), b.get(i))) return false;
        }
        return true;
    }

}
