package de.blazemcworld.fireflow.code.node.impl.string;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.StringType;
import net.minecraft.item.Items;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringLayoutNode extends Node {

    private static final Pattern pattern = Pattern.compile("\\{\\d+}");

    public StringLayoutNode() {
        super("string_layout", "String Layout", "Combines multiple strings into one given a specific layout. Use {1} for the first value in the layout, {2} for the second, etc.", Items.TRIPWIRE_HOOK);

        Input<String> layout = new Input<>("layout", "Layout", StringType.INSTANCE);
        Varargs<String> values = new Varargs<>("values", "Values", StringType.INSTANCE);

        Output<String> result = new Output<>("result", "Result", StringType.INSTANCE);

        result.valueFrom(ctx -> {
            Matcher m = pattern.matcher(layout.getValue(ctx));
            StringBuilder out = new StringBuilder();
            List<String> list = values.getVarargs(ctx);
            while (m.find()) {
                int index = Integer.parseInt(m.group(0).substring(1, m.group(0).length() - 1)) - 1;
                if (index < 0 || index >= list.size()) continue;
                m.appendReplacement(out, list.get(index));
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
