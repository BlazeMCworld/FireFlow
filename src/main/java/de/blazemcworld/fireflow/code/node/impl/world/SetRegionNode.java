package de.blazemcworld.fireflow.code.node.impl.world;

import com.mojang.serialization.DataResult;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.type.VectorType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SetRegionNode extends Node {
    public SetRegionNode() {
        super("set_region", "Set Region", "Sets a region of blocks", Items.POLISHED_ANDESITE);
        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<Vec3> corner1 = new Input<>("corner1", "Corner 1", VectorType.INSTANCE);
        Input<Vec3> corner2 = new Input<>("corner2", "Corner 2", VectorType.INSTANCE);
        Input<String> block = new Input<>("block", "Block", StringType.INSTANCE);
        Input<Boolean> sendUpdate = new Input<>("send_update", "Send Update", ConditionType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            DataResult<Block> b = Identifier.read(block.getValue(ctx)).map(BuiltInRegistries.BLOCK::getValue);
            if (b.isSuccess()) {
                BlockState state = b.getOrThrow().defaultBlockState();
                boolean updates = sendUpdate.getValue(ctx);
                int updateLimit = updates ? 512 : 0;
                int flags = updates ? Block.UPDATE_ALL : Block.UPDATE_SKIP_ALL_SIDEEFFECTS;

                Vec3 corner1Value = corner1.getValue(ctx);
                Vec3 corner2Value = corner2.getValue(ctx);

                int x1 = Mth.floor(corner1Value.x);
                int y1 = Mth.floor(corner1Value.y);
                int z1 = Mth.floor(corner1Value.z);
                int x2 = Mth.floor(corner2Value.x);
                int y2 = Mth.floor(corner2Value.y);
                int z2 = Mth.floor(corner2Value.z);

                int minX = Math.min(x1, x2);
                int maxX = Math.max(x1, x2);
                int minY = Math.min(y1, y2);
                int maxY = Math.max(y1, y2);
                int minZ = Math.min(z1, z2);
                int maxZ = Math.max(z1, z2);

                minX = Math.clamp(minX, -512, 511);
                maxX = Math.clamp(maxX, -512, 511);
                minY = Math.clamp(minY, ctx.evaluator.level.getMinY(), ctx.evaluator.level.getMaxY());
                maxY = Math.clamp(maxY, ctx.evaluator.level.getMaxY(), ctx.evaluator.level.getMaxY());
                minZ = Math.clamp(minZ, -512, 511);
                maxZ = Math.clamp(maxZ, -512, 511);

                for (BlockPos pos : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
                    ctx.evaluator.level.setBlock(pos, state, flags, updateLimit);
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