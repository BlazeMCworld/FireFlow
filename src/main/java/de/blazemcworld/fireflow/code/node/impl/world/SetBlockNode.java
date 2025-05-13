package de.blazemcworld.fireflow.code.node.impl.world;

import com.mojang.serialization.DataResult;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.type.VectorType;
import net.minecraft.block.Block;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class SetBlockNode extends Node {
    public SetBlockNode() {
        super("set_block", "Set Block", "Sets a block at a position", Items.STONE);
        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<Vec3d> position = new Input<>("position", "Position", VectorType.INSTANCE);
        Input<String> block = new Input<>("block", "Block", StringType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);
        signal.onSignal((ctx) -> {
            DataResult<Identifier> id = Identifier.validate(block.getValue(ctx));
            Optional<Block> b = id.isSuccess() ? Registries.BLOCK.getOptionalValue(id.getOrThrow()) : Optional.empty();
            if (b.isPresent()) {
                Vec3d pos = position.getValue(ctx);
                if (pos.x < -256 || pos.x > 255 || pos.z < -256 || pos.z > 255) return;
                ctx.evaluator.world.setBlockState(new BlockPos(
                        (int) Math.floor(pos.x),
                        (int) Math.floor(pos.y),
                        (int) Math.floor(pos.z)
                ), b.get().getDefaultState());
            }
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new SetBlockNode();
    }
}