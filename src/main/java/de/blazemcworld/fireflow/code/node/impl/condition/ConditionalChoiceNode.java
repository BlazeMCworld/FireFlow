package de.blazemcworld.fireflow.code.node.impl.condition;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.SingleGenericNode;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.WireType;
import net.minecraft.item.Items;

public class ConditionalChoiceNode<T> extends SingleGenericNode<T> {

    public ConditionalChoiceNode(WireType<T> type) {
        super("conditional_choice", type == null ? "Conditional Choice" : "Conditional " + type.getName() + " Choice", "Chooses between two values.", Items.WATER_BUCKET, type);

        Input<Boolean> condition = new Input<>("condition", "Condition", ConditionType.INSTANCE);
        Input<T> trueValue = new Input<>("trueValue", "True Value", type);
        Input<T> falseValue = new Input<>("falseValue", "False Value", type);
        Output<T> choice = new Output<>("choice", "Choice", type);

        choice.valueFrom(ctx -> {
            if (condition.getValue(ctx)) return trueValue.getValue(ctx);
            return falseValue.getValue(ctx);
        });
    }

    @Override
    public Node copy() {
        return new ConditionalChoiceNode<>(type);
    }

    @Override
    public Node copyWithType(WireType<?> type) {
        return new ConditionalChoiceNode<>(type);
    }
}
