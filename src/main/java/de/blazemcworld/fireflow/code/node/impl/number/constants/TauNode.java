package de.blazemcworld.fireflow.code.node.impl.number.constants;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import net.minecraft.item.Items;

public class TauNode extends Node {

    public TauNode() {
        super("tau", "Tau (τ)", "Returns TAU.", Items.SNOW_BLOCK);

        Output<Double> value = new Output<>("value", "Value", NumberType.INSTANCE);
        value.valueFrom(ctx -> Math.TAU);
    }

    @Override
    public Node copy() {
        return new TauNode();
    }

}
