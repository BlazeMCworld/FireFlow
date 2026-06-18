package de.blazemcworld.fireflow.code.node.impl.world;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import net.minecraft.item.Items;

public class TimestampNode extends Node {

    public TimestampNode() {
        super("timestamp", "Timestamp", "Returns the elapsed seconds since the Unix epoch.", Items.CLOCK);

        Output<Double> usage = new Output<>("timestamp", "Timestamp", NumberType.INSTANCE);
        usage.valueFrom(ctx -> (double) System.currentTimeMillis() / 1000);
    }

    @Override
    public Node copy() {
        return new TimestampNode();
    }

}
