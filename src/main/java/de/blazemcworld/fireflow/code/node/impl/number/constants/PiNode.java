package de.blazemcworld.fireflow.code.node.impl.number.constants;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import net.minecraft.item.Items;

public class PiNode extends Node {

    public PiNode() {
        super("pi", "Pi (π)", "Returns PI.", Items.SNOWBALL);

        Output<Double> value = new Output<>("value", "Value", NumberType.INSTANCE);
        value.valueFrom(ctx -> Math.PI);
    }

    @Override
    public Node copy() {
        return new PiNode();
    }

}
