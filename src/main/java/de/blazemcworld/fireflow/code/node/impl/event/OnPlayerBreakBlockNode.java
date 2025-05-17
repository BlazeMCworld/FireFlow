package de.blazemcworld.fireflow.code.node.impl.event;

import de.blazemcworld.fireflow.code.CodeEvaluator;
import de.blazemcworld.fireflow.code.CodeThread;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.VectorType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class OnPlayerBreakBlockNode extends Node {

    private final Output<Void> signal;
    private final Output<PlayerValue> player;
    private final Output<Vec3d> position;

    public OnPlayerBreakBlockNode() {
        super("on_player_break_block", "On Player Break Block", "Emits a signal when a player breaks a block.", Items.IRON_PICKAXE);

        signal = new Output<>("signal", "Signal", SignalType.INSTANCE);
        player = new Output<>("player", "Player", PlayerType.INSTANCE);
        position = new Output<>("position", "Position", VectorType.INSTANCE);

        player.valueFromScope();
        position.valueFromScope();
    }

    @Override
    public Node copy() {
        return new OnPlayerBreakBlockNode();
    }

    public boolean onBreakBlock(CodeEvaluator codeEvaluator, ServerPlayerEntity player, BlockPos pos, boolean cancel) {
        CodeThread thread = codeEvaluator.newCodeThread();
        thread.eventCancelled = cancel;
        thread.setScopeValue(this.player, new PlayerValue(player));
        thread.setScopeValue(this.position, Vec3d.ofCenter(pos));
        thread.sendSignal(signal);
        thread.clearQueue();
        return thread.eventCancelled;
    }
}
