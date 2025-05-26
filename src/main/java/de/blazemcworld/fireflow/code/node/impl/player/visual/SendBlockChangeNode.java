package de.blazemcworld.fireflow.code.node.impl.player.visual;

import com.mojang.serialization.DataResult;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.*;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.block.Block;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class SendBlockChangeNode extends Node {
    public SendBlockChangeNode() {
        super("send_block_change", "Send Block Change", "Sends a fake block change packet", Items.AXOLOTL_BUCKET);
        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<String> block = new Input<>("block", "Block", StringType.INSTANCE);
        Input<Vec3d> position = new Input<>("position", "Position", VectorType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);
        signal.onSignal((ctx) -> {
            DataResult<Identifier> id = Identifier.validate(block.getValue(ctx));
            Optional<Block> placedBlock = id.isSuccess() ? Registries.BLOCK.getOptionalValue(id.getOrThrow()) : Optional.empty();
            if (placedBlock.isPresent()) {
                Vec3d pos = position.getValue(ctx);
                player.getValue(ctx).tryUse(ctx, p -> p.networkHandler.sendPacket(new BlockUpdateS2CPacket(
                        BlockPos.ofFloored(pos), placedBlock.get().getDefaultState()
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