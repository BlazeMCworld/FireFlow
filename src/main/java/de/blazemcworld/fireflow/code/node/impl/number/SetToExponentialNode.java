package de.blazemcworld.fireflow.code.node.impl.number;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import net.minecraft.item.Items;

public class SetToExponentialNode extends Node {
    public SetToExponentialNode() {
        super("set_to_exponential", "Exponential", "Raises a number to a power", Items.CHEST.asItem());
        Input<Double> base = new Input<>("base", "Base", NumberType.INSTANCE);
        Input<Double> exponent = new Input<>("exponent", "Exponent", NumberType.INSTANCE);
        Output<Double> result = new Output<>("result", "Result", NumberType.INSTANCE);

        result.valueFrom((ctx -> Math.pow(base.getValue(ctx), exponent.getValue(ctx))));
    }

    @Override
    public Node copy() {
        return new SetToExponentialNode();
    }
}