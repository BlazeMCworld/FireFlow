package de.blazemcworld.fireflow.code.node.impl.event.action;

import de.blazemcworld.fireflow.code.CodeEvaluator;
import de.blazemcworld.fireflow.code.CodeThread;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

public class OnPlayerStopSprintingNode extends Node {

    private final Output<Void> signal;
    private final Output<PlayerValue> player;

    public OnPlayerStopSprintingNode() {
        super("on_player_stop_sprinting", "On Player Stop Sprinting", "Emits a signal when a player stops to sprint.", Items.LEATHER_BOOTS);

        signal = new Output<>("signal", "Signal", SignalType.INSTANCE);
        player = new Output<>("player", "Player", PlayerType.INSTANCE);
        player.valueFromScope();
    }

    public void emit(CodeEvaluator evaluator, ServerPlayerEntity player) {
        CodeThread thread = evaluator.newCodeThread();
        thread.setScopeValue(this.player, new PlayerValue(player));
        thread.sendSignal(signal);
        thread.clearQueue();
    }

    @Override
    public Node copy() {
        return new OnPlayerStopSprintingNode();
    }

}
