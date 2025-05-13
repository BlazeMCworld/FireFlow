package de.blazemcworld.fireflow.code.node.impl.condition;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import net.minecraft.item.Items;

public class InvertConditionNode extends Node {

    public InvertConditionNode() {
        super("invert_condition", "Invert Condition", "Inverts a condition.", Items.REDSTONE_TORCH);

        Input<Boolean> normal = new Input<>("normal", "Normal", ConditionType.INSTANCE);
        Output<Boolean> inverted = new Output<>("inverted", "Inverted", ConditionType.INSTANCE);

        inverted.valueFrom((ctx) -> !normal.getValue(ctx));
    }

    @Override
    public Node copy() {
        return new InvertConditionNode();
    }
}
