package de.blazemcworld.fireflow.code.node.impl.string;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.AnyType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.value.AnyValue;
import net.minecraft.item.Items;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringLayoutNode extends Node {

    private static final Pattern pattern = Pattern.compile("\\{\\d+(:\\w+)*}");

    public StringLayoutNode() {
        super("string_layout", "String Layout", "Combines multiple strings into one given a specific layout. Use {1} for the first value in the layout, {2} for the second, etc.", Items.TRIPWIRE_HOOK);

        Input<String> layout = new Input<>("layout", "Layout", StringType.INSTANCE);
        Varargs<AnyValue<?>> values = new Varargs<>("values", "Values", AnyType.INSTANCE);

        Output<String> result = new Output<>("result", "Result", StringType.INSTANCE);

        result.valueFrom(ctx -> {
            Matcher m = pattern.matcher(layout.getValue(ctx));
            StringBuilder out = new StringBuilder();
            List<AnyValue<?>> list = values.getVarargs(ctx);
            while (m.find()) {
                String id = m.group().substring(1, m.group().length() - 1);
                String format = "display";
                int splitIndex = id.indexOf(':');
                if (splitIndex != -1) {
                    format = id.substring(splitIndex + 1);
                    id = id.substring(0, splitIndex);
                }
                int index = Integer.parseInt(id) - 1;
                if (index < 0 || index >= list.size()) continue;
                m.appendReplacement(out, AnyType.INSTANCE.stringify(list.get(index), format));
            }
            m.appendTail(out);
            return out.toString();
        });
    }

    @Override
    public Node copy() {
        return new StringLayoutNode();
    }

}
