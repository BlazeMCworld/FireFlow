package de.blazemcworld.fireflow.code.node.impl.world;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.type.VectorType;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class RaycastNode extends Node {

    public RaycastNode() {
        super("raycast", "Raycast", "Sends out a raycast, like a ray of light and returns if, where and what it hits.", Items.SPECTRAL_ARROW);

        Input<Vec3d> start = new Input<>("start", "Start", VectorType.INSTANCE);
        Input<Vec3d> end = new Input<>("end", "End", VectorType.INSTANCE);
        Input<Boolean> fluids = new Input<>("fluids", "Fluids", ConditionType.INSTANCE);
        Output<Vec3d> point = new Output<>("point", "Point", VectorType.INSTANCE);
        Output<String> block = new Output<>("block", "Block", StringType.INSTANCE);
        Output<Vec3d> side = new Output<>("side", "Side", VectorType.INSTANCE);

        point.valueFrom(ctx -> {
            Vec3d startVec = start.getValue(ctx);
            Vec3d endVec = end.getValue(ctx);
            BlockHitResult result = ctx.evaluator.world.raycast(new RaycastContext(
                    startVec, endVec,
                    RaycastContext.ShapeType.COLLIDER,
                    fluids.getValue(ctx) ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE,
                    ShapeContext.absent()
            ));
            if (result.getType() == HitResult.Type.MISS) return endVec;
            return result.getPos();
        });

        block.valueFrom(ctx -> {
            Vec3d startVec = start.getValue(ctx);
            Vec3d endVec = end.getValue(ctx);
            BlockHitResult result = ctx.evaluator.world.raycast(new RaycastContext(
                    startVec, endVec,
                    RaycastContext.ShapeType.COLLIDER,
                    fluids.getValue(ctx) ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE,
                    ShapeContext.absent()
            ));
            if (result.getType() == HitResult.Type.MISS) {
                return Registries.BLOCK.getId(ctx.evaluator.world.getBlockState(BlockPos.ofFloored(endVec)).getBlock()).getPath();
            }
            return Registries.BLOCK.getId(ctx.evaluator.world.getBlockState(result.getBlockPos()).getBlock()).getPath();
        });

        side.valueFrom(ctx -> {
            Vec3d startVec = start.getValue(ctx);
            Vec3d endVec = end.getValue(ctx);
            BlockHitResult result = ctx.evaluator.world.raycast(new RaycastContext(
                    startVec, endVec,
                    RaycastContext.ShapeType.COLLIDER,
                    fluids.getValue(ctx) ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE,
                    ShapeContext.absent()
            ));
            if (result.getType() == HitResult.Type.MISS) return Vec3d.ZERO;
            return result.getSide().getDoubleVector();
        });
    }

    @Override
    public Node copy() {
        return new RaycastNode();
    }
}
