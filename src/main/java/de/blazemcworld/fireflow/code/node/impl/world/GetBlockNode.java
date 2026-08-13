package de.blazemcworld.fireflow.code.node.impl.world;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.type.VectorType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class GetBlockNode extends Node {
    public GetBlockNode() {
        super("get_block", "Get Block", "Gets the block at a position", Items.ENDER_EYE);

        Input<Vec3> position = new Input<>("position", "Position", VectorType.INSTANCE);
        Output<String> block = new Output<>("block", "Block", StringType.INSTANCE);

        block.valueFrom((ctx) -> {
            Vec3 pos = position.getValue(ctx);
            return BuiltInRegistries.BLOCK.getKey(ctx.evaluator.level.getBlockState(BlockPos.containing(pos)).getBlock()).getPath();
        });
    }

    @Override
    public Node copy() {
        return new GetBlockNode();
    }
}