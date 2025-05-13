package de.blazemcworld.fireflow.code.node.impl.flow;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.VectorType;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

public class GridRepeatNode extends Node {

    public GridRepeatNode() {
        super("grid_repeat", "Grid Repeat", "Emits a signal for each block position in a region. If relative end is enabled, the end is an offset from the start.", Items.DARK_PRISMARINE);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<Vec3d> start = new Input<>("start", "Start", VectorType.INSTANCE);
        Input<Vec3d> end = new Input<>("end", "End", VectorType.INSTANCE);
        Input<Boolean> relativeEnd = new Input<>("relative", "Relative End", ConditionType.INSTANCE);
        Output<Void> repeat = new Output<>("repeat", "Repeat", SignalType.INSTANCE);
        Output<Vec3d> current = new Output<>("current", "Current", VectorType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);
        current.valueFromScope();

        signal.onSignal((ctx) -> {
            Vec3d startValue = start.getValue(ctx);
            Vec3d endValue = relativeEnd.getValue(ctx) ? startValue.add(end.getValue(ctx)) : end.getValue(ctx);

            int minX = (int) Math.max(-256, Math.min(startValue.x, endValue.x));
            int minY = (int) Math.max(ctx.evaluator.world.getBottomY(), Math.min(startValue.y, endValue.y));
            int minZ = (int) Math.max(-256, Math.min(startValue.z, endValue.z));
            int maxX = (int) Math.min(255, Math.max(startValue.x, endValue.x));
            int maxY = (int) Math.min(ctx.evaluator.world.getTopYInclusive() - 1, Math.max(startValue.y, endValue.y));
            int maxZ = (int) Math.min(255, Math.max(startValue.z, endValue.z));

            Vec3d[] val = new Vec3d[]{new Vec3d(minX, minY, minZ)};
            ctx.setScopeValue(current, val[0]);

            ctx.submit(new Runnable() {
                @Override
                public void run() {
                    if (val[0].x >= maxX) {
                        val[0] = new Vec3d(minX, val[0].y + 1, minZ);
                    } else val[0] = val[0].add(1, 0, 0);

                    if (val[0].y >= maxY) {
                        val[0] = new Vec3d(minX, minY, val[0].z + 1);
                    }

                    if (val[0].z >= maxZ) {
                        ctx.sendSignal(next);
                        return;
                    }

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
