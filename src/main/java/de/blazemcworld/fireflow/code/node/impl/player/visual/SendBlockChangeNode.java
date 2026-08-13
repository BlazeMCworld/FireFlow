package de.blazemcworld.fireflow.code.node.impl.player.visual;

import com.mojang.serialization.DataResult;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.type.VectorType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class SendBlockChangeNode extends Node {
    public SendBlockChangeNode() {
        super("send_block_change", "Send Block Change", "Sends a fake block change packet", Items.AXOLOTL_BUCKET);
        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<String> block = new Input<>("block", "Block", StringType.INSTANCE);
        Input<Vec3> position = new Input<>("position", "Position", VectorType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);
        signal.onSignal((ctx) -> {
            DataResult<Identifier> id = Identifier.read(block.getValue(ctx));
            Optional<Holder.Reference<Block>> placedBlock = id.isSuccess() ? BuiltInRegistries.BLOCK.get(id.getOrThrow()) : Optional.empty();
            if (placedBlock.isPresent()) {
                Vec3 pos = position.getValue(ctx);
                player.getValue(ctx).tryUse(ctx, p -> p.connection.send(new ClientboundBlockUpdatePacket(
                        BlockPos.containing(pos), placedBlock.get().value().defaultBlockState()
                )));
            }
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new SendBlockChangeNode();
    }
}