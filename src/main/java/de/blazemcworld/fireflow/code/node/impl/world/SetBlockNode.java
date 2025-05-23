package de.blazemcworld.fireflow.code.node.impl.world;

import com.mojang.serialization.DataResult;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
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
        Input<Boolean> sendUpdate = new Input<>("send_update", "Send Update", ConditionType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            DataResult<Identifier> id = Identifier.validate(block.getValue(ctx));
            Optional<Block> b = id.isSuccess() ? Registries.BLOCK.getOptionalValue(id.getOrThrow()) : Optional.empty();
            if (b.isPresent()) {
                Vec3d pos = position.getValue(ctx);
                if (pos.x < -512 || pos.x > 511 || pos.z < -512 || pos.z > 511 || pos.y < ctx.evaluator.world.getBottomY() || pos.y > ctx.evaluator.world.getTopYInclusive()) return;
                ctx.evaluator.world.setBlockState(BlockPos.ofFloored(pos), b.get().getDefaultState(), sendUpdate.getValue(ctx) ? Block.NOTIFY_ALL : 0);
            }
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new SetBlockNode();
    }
}