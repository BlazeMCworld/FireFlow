package de.blazemcworld.fireflow.code.node.impl.world;

import com.mojang.serialization.DataResult;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.type.VectorType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class SetRegionNode extends Node {
    public SetRegionNode() {
        super("set_region", "Set Region", "Sets a region of blocks", Items.POLISHED_ANDESITE);
        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<Vec3d> corner1 = new Input<>("corner1", "Corner 1", VectorType.INSTANCE);
        Input<Vec3d> corner2 = new Input<>("corner2", "Corner 2", VectorType.INSTANCE);
        Input<String> block = new Input<>("block", "Block", StringType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            DataResult<Identifier> id = Identifier.validate(block.getValue(ctx));
            Optional<Block> placedBlock = id.isSuccess() ? Registries.BLOCK.getOptionalValue(id.getOrThrow()) : Optional.empty();
            if (placedBlock.isPresent()) {
                Vec3d corner1Value = corner1.getValue(ctx);
                Vec3d corner2Value = corner2.getValue(ctx);
                corner1Value = new Vec3d(
                        Math.floor(corner1Value.x),
                        Math.floor(corner1Value.y),
                        Math.floor(corner1Value.z)
                );
                corner2Value = new Vec3d(
                        Math.floor(corner2Value.x),
                        Math.floor(corner2Value.y),
                        Math.floor(corner2Value.z)
                );

                int minX = (int) Math.max(-256, Math.min(corner1Value.x, corner2Value.x));
                int minY = (int) Math.max(ctx.evaluator.world.getBottomY(), Math.min(corner1Value.y, corner2Value.y));
                int minZ = (int) Math.max(-256, Math.min(corner1Value.z, corner2Value.z));
                int maxX = (int) Math.min(255, Math.max(corner1Value.x, corner2Value.x));
                int maxY = (int) Math.min(ctx.evaluator.world.getTopYInclusive() - 1, Math.max(corner1Value.y, corner2Value.y));
                int maxZ = (int) Math.min(255, Math.max(corner1Value.z, corner2Value.z));

                BlockState state = placedBlock.get().getDefaultState();
                for (BlockPos pos : BlockPos.iterate(minX, minY, minZ, maxX, maxY, maxZ)) {
                    ctx.evaluator.world.setBlockState(pos, state);
                }
            }
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new SetRegionNode();
    }
}