package de.blazemcworld.fireflow.code.node.impl.condition;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import net.minecraft.item.Items;

public class ConditionAndNode extends Node {

    public ConditionAndNode() {
        super("conditional_and", "Conditional And", "Checks if all conditions are true.", Items.REDSTONE_BLOCK);

        Input<Boolean> primary = new Input<>("primary", "Primary", ConditionType.INSTANCE);
        Varargs<Boolean> others = new Varargs<>("others", "Others", ConditionType.INSTANCE);
        Output<Boolean> result = new Output<>("result", "Result", ConditionType.INSTANCE);

        result.valueFrom((ctx) -> {
            if (!primary.getValue(ctx)) return false;
            return !others.getVarargs(ctx).contains(false);
        });
    }

    @Override
    public Node copy() {
        return new ConditionAndNode();
    }
}
