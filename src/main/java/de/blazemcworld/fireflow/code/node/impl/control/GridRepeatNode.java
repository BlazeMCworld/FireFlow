package de.blazemcworld.fireflow.code.node.impl.control;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.type.VectorType;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class GridRepeatNode extends Node {

    public GridRepeatNode() {
        super("grid_repeat", "Grid Repeat", "Emits a signal for each block vector in an area. If relative end is enabled, the end is added as an offset to the start.", Items.DARK_PRISMARINE);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<Vec3> start = new Input<>("start", "Start", VectorType.INSTANCE);
        Input<Vec3> end = new Input<>("end", "End", VectorType.INSTANCE);
        Input<String> mode = new Input<>("mode", "Mode", StringType.INSTANCE).options("absolute_end", "relative_end");
        Output<Void> repeat = new Output<>("repeat", "Repeat", SignalType.INSTANCE);
        Output<Vec3> current = new Output<>("current", "Current", VectorType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);
        current.valueFromScope();

        signal.onSignal((ctx) -> {
            Vec3 startValue = start.getValue(ctx);
            Vec3 endValue = mode.getValue(ctx).equals("relative_end") ? startValue.add(end.getValue(ctx)) : end.getValue(ctx);

            double minX = Math.min(startValue.x, endValue.x);
            double maxX = Math.max(startValue.x, endValue.x);
            double minY = Math.min(startValue.y, endValue.y);
            double maxY = Math.max(startValue.y, endValue.y);
            double minZ = Math.min(startValue.z, endValue.z);
            double maxZ = Math.max(startValue.z, endValue.z);

            Vec3[] val = new Vec3[]{new Vec3(minX, minY, minZ)};
            ctx.setScopeValue(current, val[0]);

            ctx.submit(new Runnable() {
                @Override
                public void run() {
                    val[0] = val[0].add(1, 0, 0);
                    if (val[0].x > maxX) {
                        val[0] = new Vec3(minX, val[0].y + 1, val[0].z);

                        if (val[0].y > maxY) {
                            val[0] = new Vec3(minX, minY, val[0].z + 1);

                            if (val[0].z > maxZ) {
                                ctx.sendSignal(next);
                                return;
                            }
                        }
                    }

                    ctx.setScopeValue(current, val[0]);
                    ctx.submit(this);
                    ctx.sendSignal(repeat);
                }
            });
            ctx.sendSignal(repeat);
        });
    }

    @Override
    public Node copy() {
        return new GridRepeatNode();
    }

}
