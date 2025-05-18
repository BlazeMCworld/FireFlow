package de.blazemcworld.fireflow.code.node.impl.world;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.type.VectorType;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class GetBlockNode extends Node {
    public GetBlockNode() {
        super("get_block", "Get Block", "Gets the block at a position", Items.ENDER_EYE);

        Input<Vec3d> position = new Input<>("position", "Position", VectorType.INSTANCE);
        Output<String> block = new Output<>("block", "Block", StringType.INSTANCE);

        block.valueFrom((ctx) -> {
            Vec3d pos = position.getValue(ctx);
            return Registries.BLOCK.getId(ctx.evaluator.world.getBlockState(BlockPos.ofFloored(pos)).getBlock()).getPath();
        });
    }

    @Override
    public Node copy() {
        return new GetBlockNode();
    }
}