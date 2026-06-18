package de.blazemcworld.fireflow.code.node.impl.number.constants;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import net.minecraft.item.Items;

public class GoldenRatioNode extends Node {

    public GoldenRatioNode() {
        super("golden_ratio", "Golden Ratio (Φ)", "Returns the Golden Ratio.", Items.GOLD_NUGGET);

        Output<Double> value = new Output<>("value", "Value", NumberType.INSTANCE);
        value.valueFrom(ctx -> (1 + Math.sqrt(5)) / 2);
    }

    @Override
    public Node copy() {
        return new GoldenRatioNode();
    }

}
