package de.blazemcworld.fireflow.code.node.impl.condition;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import net.minecraft.item.Items;

public class ConditionOrNode extends Node {

    public ConditionOrNode() {
        super("conditional_or", "Conditional Or", "Checks if at least one condition is true.", Items.REDSTONE_ORE);

        Input<Boolean> primary = new Input<>("primary", "Primary", ConditionType.INSTANCE);
        Varargs<Boolean> others = new Varargs<>("others", "Others", ConditionType.INSTANCE);
        Output<Boolean> result = new Output<>("result", "Result", ConditionType.INSTANCE);

        result.valueFrom((ctx) -> {
            if (primary.getValue(ctx)) return true;
            return others.getVarargs(ctx).contains(true);
        });
    }

    @Override
    public Node copy() {
        return new ConditionOrNode();
    }
}
