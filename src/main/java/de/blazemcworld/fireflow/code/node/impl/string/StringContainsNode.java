package de.blazemcworld.fireflow.code.node.impl.string;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.StringType;
import net.minecraft.item.Items;

public class StringContainsNode extends Node {

    public StringContainsNode() {
        super("string_contains", "String Contains", "Checks if a string contains another string", Items.GLASS_BOTTLE);

        Input<String> string = new Input<>("string", "String", StringType.INSTANCE);
        Input<String> part = new Input<>("part", "Part", StringType.INSTANCE);
        Input<Boolean> ignoreCase = new Input<>("ignore_case", "Ignore Case", ConditionType.INSTANCE);
        Output<Boolean> contains = new Output<>("contains", "Contains", ConditionType.INSTANCE);

        contains.valueFrom(ctx -> {
            String str = string.getValue(ctx);
            String partStr = part.getValue(ctx);
            if (ignoreCase.getValue(ctx)) {
                str = str.toLowerCase();
                partStr = partStr.toLowerCase();
            }
            return str.contains(partStr);
        });
    }

    @Override
    public Node copy() {
        return new StringContainsNode();
    }
}
