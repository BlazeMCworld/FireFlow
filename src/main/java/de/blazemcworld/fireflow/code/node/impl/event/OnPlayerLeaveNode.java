package de.blazemcworld.fireflow.code.node.impl.event;

import de.blazemcworld.fireflow.code.CodeEvaluator;
import de.blazemcworld.fireflow.code.CodeThread;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

public class OnPlayerLeaveNode extends Node {

    private final Output<Void> signal;
    private final Output<PlayerValue> player;

    public OnPlayerLeaveNode() {
        super("on_player_leave", "On Player Leave", "Emits a signal when a player leaves.", Items.IRON_DOOR);

        signal = new Output<>("signal", "Signal", SignalType.INSTANCE);
        player = new Output<>("player", "Player", PlayerType.INSTANCE);
        player.valueFromScope();
    }

    @Override
    public Node copy() {
        return new OnPlayerLeaveNode();
    }

    public void onLeave(CodeEvaluator codeEvaluator, ServerPlayerEntity player) {
        CodeThread thread = codeEvaluator.newCodeThread();
        thread.setScopeValue(this.player, new PlayerValue(player));
        thread.sendSignal(signal);
        thread.clearQueue();
    }
}
