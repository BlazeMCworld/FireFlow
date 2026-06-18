package de.blazemcworld.fireflow.code.node.impl.number.constants;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import net.minecraft.item.Items;

public class EulersNode extends Node {

    public EulersNode() {
        super("eulers", "Euler's (e)", "Returns Euler's number.", Items.SPECTRAL_ARROW);

        Output<Double> value = new Output<>("value", "Value", NumberType.INSTANCE);
        value.valueFrom(ctx -> Math.E);
    }

    @Override
    public Node copy() {
        return new EulersNode();
    }

}
