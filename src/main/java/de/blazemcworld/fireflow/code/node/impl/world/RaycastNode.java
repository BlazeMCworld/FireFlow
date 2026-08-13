package de.blazemcworld.fireflow.code.node.impl.world;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.type.VectorType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public class RaycastNode extends Node {

    public RaycastNode() {
        super("raycast", "Raycast", "Sends out a raycast, like a ray of light and returns if, where and what it hits.", Items.SPECTRAL_ARROW);

        Input<Vec3> start = new Input<>("start", "Start", VectorType.INSTANCE);
        Input<Vec3> end = new Input<>("end", "End", VectorType.INSTANCE);
        Input<Boolean> fluids = new Input<>("fluids", "Fluids", ConditionType.INSTANCE);
        Output<Vec3> point = new Output<>("point", "Point", VectorType.INSTANCE);
        Output<String> block = new Output<>("block", "Block", StringType.INSTANCE);
        Output<Vec3> side = new Output<>("side", "Side", VectorType.INSTANCE);

        point.valueFrom(ctx -> {
            Vec3 startVec = start.getValue(ctx);
            Vec3 endVec = end.getValue(ctx);
            BlockHitResult result = ctx.evaluator.level.clip(new ClipContext(
                    startVec, endVec,
                    ClipContext.Block.COLLIDER,
                    fluids.getValue(ctx) ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE,
                    CollisionContext.empty()
            ));
            if (result.getType() == BlockHitResult.Type.MISS) return endVec;
            return result.getLocation();
        });

        block.valueFrom(ctx -> {
            Vec3 startVec = start.getValue(ctx);
            Vec3 endVec = end.getValue(ctx);
            BlockHitResult result = ctx.evaluator.level.clip(new ClipContext(
                    startVec, endVec,
                    ClipContext.Block.COLLIDER,
                    fluids.getValue(ctx) ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE,
                    CollisionContext.empty()
            ));
            if (result.getType() == BlockHitResult.Type.MISS) {
                return BuiltInRegistries.BLOCK.getKey(ctx.evaluator.level.getBlockState(BlockPos.containing(endVec)).getBlock()).getPath();
            }
            return BuiltInRegistries.BLOCK.getKey(ctx.evaluator.level.getBlockState(result.getBlockPos()).getBlock()).getPath();
        });

        side.valueFrom(ctx -> {
            Vec3 startVec = start.getValue(ctx);
            Vec3 endVec = end.getValue(ctx);
            BlockHitResult result = ctx.evaluator.level.clip(new ClipContext(
                    startVec, endVec,
                    ClipContext.Block.COLLIDER,
                    fluids.getValue(ctx) ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE,
                    CollisionContext.empty()
            ));
            if (result.getType() == BlockHitResult.Type.MISS) return Vec3.ZERO;
            return result.getDirection().getUnitVec3();
        });
    }

    @Override
    public Node copy() {
        return new RaycastNode();
    }
}
