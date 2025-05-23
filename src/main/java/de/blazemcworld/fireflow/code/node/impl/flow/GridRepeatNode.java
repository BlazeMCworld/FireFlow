package de.blazemcworld.fireflow.code.node.impl.flow;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.type.VectorType;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class GridRepeatNode extends Node {

    public GridRepeatNode() {
        super("grid_repeat", "Grid Repeat", "Emits a signal for each block position in a region. If relative end is enabled, the end is an offset from the start.", Items.DARK_PRISMARINE);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<Vec3d> start = new Input<>("start", "Start", VectorType.INSTANCE);
        Input<Vec3d> end = new Input<>("end", "End", VectorType.INSTANCE);
        Input<String> mode = new Input<>("mode", "Mode", StringType.INSTANCE).options("absolute_end", "relative_end");
        Output<Void> repeat = new Output<>("repeat", "Repeat", SignalType.INSTANCE);
        Output<Vec3d> current = new Output<>("current", "Current", VectorType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);
        current.valueFromScope();

        signal.onSignal((ctx) -> {
            Vec3d startValue = start.getValue(ctx);
            Vec3d endValue = mode.getValue(ctx).equals("relative_end") ? startValue.add(end.getValue(ctx)) : end.getValue(ctx);

            int minX = MathHelper.floor(Math.max(-512, Math.min(startValue.x, endValue.x)));
            int minY = MathHelper.floor(Math.max(ctx.evaluator.world.getBottomY(), Math.min(startValue.y, endValue.y)));
            int minZ = MathHelper.floor(Math.max(-512, Math.min(startValue.z, endValue.z)));
            int maxX = MathHelper.floor(Math.min(511, Math.max(startValue.x, endValue.x)));
            int maxY = MathHelper.floor(Math.min(ctx.evaluator.world.getTopYInclusive() - 1, Math.max(startValue.y, endValue.y)));
            int maxZ = MathHelper.floor(Math.min(511, Math.max(startValue.z, endValue.z)));

            Vec3d[] val = new Vec3d[]{new Vec3d(minX, minY, minZ)};
            ctx.setScopeValue(current, val[0]);

            ctx.submit(new Runnable() {
                @Override
                public void run() {
                    val[0] = val[0].add(1, 0, 0);

                    if (val[0].x > maxX) {
                        val[0] = new Vec3d(minX, val[0].y + 1, val[0].z);
                    }

                    if (val[0].y > maxY) {
                        val[0] = new Vec3d(minX, minY, val[0].z + 1);
                    }

                    if (val[0].z > maxZ) {
                        ctx.sendSignal(next);
                        return;
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
