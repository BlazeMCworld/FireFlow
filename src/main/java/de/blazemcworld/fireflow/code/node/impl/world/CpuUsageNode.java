package de.blazemcworld.fireflow.code.node.impl.world;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import net.minecraft.item.Items;

public class CpuUsageNode extends Node {

    public CpuUsageNode() {
        super("cpu_usage", "CPU Usage", "Returns the spaces CPU usage in percent.", Items.REDSTONE_LAMP);

        Output<Double> usage = new Output<>("usage", "Usage", NumberType.INSTANCE);
        usage.valueFrom(ctx -> ctx.evaluator.world.cpuMs() / 10.0);
    }

    @Override
    public Node copy() {
        return new CpuUsageNode();
    }

}
