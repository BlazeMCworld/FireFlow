package de.blazemcworld.fireflow.code.node.impl.string;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.StringType;
import net.minecraft.item.Items;

import java.util.List;

public class CombineStringsNode extends Node {
    public CombineStringsNode() {
        super("combine_strings", "Combine Strings", "Appends multiple strings together", Items.SLIME_BALL);

        Input<String> separator = new Input<>("separator", "Separator", StringType.INSTANCE);
        Varargs<String> strings = new Varargs<>("strings", "Strings", StringType.INSTANCE);
        Output<String> combined = new Output<>("combined", "Combined", StringType.INSTANCE);

        combined.valueFrom(ctx -> {
            StringBuilder out = new StringBuilder();
            String sep = separator.getValue(ctx);
            List<String> strs = strings.getVarargs(ctx);
            for (int i = 0; i < strs.size(); i++) {
                if (i > 0) out.append(sep);
                out.append(strs.get(i));
            }

            return out.toString();
        });
    }

    @Override
    public Node copy() {
        return new CombineStringsNode();
    }
}
