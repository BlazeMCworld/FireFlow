package de.blazemcworld.fireflow.code.node.impl.world;

import com.mojang.serialization.DataResult;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.type.VectorType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class SetBlockNode extends Node {
    public SetBlockNode() {
        super("set_block", "Set Block", "Sets a block at a position", Items.STONE);
        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<Vec3> position = new Input<>("position", "Position", VectorType.INSTANCE);
        Input<String> block = new Input<>("block", "Block", StringType.INSTANCE);
        Input<Boolean> sendUpdate = new Input<>("send_update", "Send Update", ConditionType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            DataResult<Identifier> id = Identifier.read(block.getValue(ctx));
            Optional<Holder.Reference<Block>> b = id.isSuccess() ? BuiltInRegistries.BLOCK.get(id.getOrThrow()) : Optional.empty();
            if (b.isPresent()) {
                Vec3 pos = position.getValue(ctx);
                if (pos.x < -512 || pos.x > 511 || pos.z < -512 || pos.z > 511 || pos.y < ctx.evaluator.level.getMinY() || pos.y > ctx.evaluator.level.getMaxY()) {
                    ctx.sendSignal(next);
                    return;
                }
                boolean updates = sendUpdate.getValue(ctx);
                int updateLimit = updates ? 512 : 0;
                int flags = updates ? Block.UPDATE_ALL : Block.UPDATE_SKIP_ALL_SIDEEFFECTS;
                ctx.evaluator.level.setBlock(BlockPos.containing(pos), b.get().value().defaultBlockState(), flags, updateLimit);
            }
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new SetBlockNode();
    }
}