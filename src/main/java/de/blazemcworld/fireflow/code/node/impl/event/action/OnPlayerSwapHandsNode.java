package de.blazemcworld.fireflow.code.node.impl.event.action;

import de.blazemcworld.fireflow.code.CodeEvaluator;
import de.blazemcworld.fireflow.code.CodeThread;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

public class OnPlayerSwapHandsNode extends Node {

    private final Output<Void> signal;
    private final Output<PlayerValue> player;

    public OnPlayerSwapHandsNode() {
        super("on_player_swap_hands", "On Player Swap Hands", "Emits a signal when a player swaps their hand items.", Items.SHIELD);

        signal = new Output<>("signal", "Signal", SignalType.INSTANCE);
        player = new Output<>("player", "Player", PlayerType.INSTANCE);
        player.valueFromScope();
    }

    public boolean onSwapHands(CodeEvaluator codeEvaluator, ServerPlayerEntity player, boolean cancel) {
        CodeThread thread = codeEvaluator.newCodeThread();
        thread.context.cancelled = cancel;
        thread.setScopeValue(this.player, new PlayerValue(player));
        thread.sendSignal(signal);
        thread.clearQueue();
        return thread.context.cancelled;
    }

    @Override
    public Node copy() {
        return new OnPlayerSwapHandsNode();
    }
}

