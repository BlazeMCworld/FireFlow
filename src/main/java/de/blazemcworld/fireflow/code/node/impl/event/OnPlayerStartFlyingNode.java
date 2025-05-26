package de.blazemcworld.fireflow.code.node.impl.event;

import de.blazemcworld.fireflow.code.CodeEvaluator;
import de.blazemcworld.fireflow.code.CodeThread;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

public class OnPlayerStartFlyingNode extends Node {

    private final Output<Void> signal;
    private final Output<PlayerValue> player;

    public OnPlayerStartFlyingNode() {
        super("on_player_start_fly", "On Player Start Fly", "Emits a signal when a player starts flying (not gliding with an elytra).", Items.FEATHER);

        signal = new Output<>("signal", "Signal", SignalType.INSTANCE);
        player = new Output<>("player", "Player", PlayerType.INSTANCE);
        player.valueFromScope();

    }

    @Override
    public Node copy() {
        return new OnPlayerStartFlyingNode();
    }

    public boolean onStartFlying(CodeEvaluator evaluator, ServerPlayerEntity player, boolean cancel) {
        CodeThread thread = evaluator.newCodeThread();
        thread.context.cancelled = cancel;
        thread.setScopeValue(this.player, new PlayerValue(player));
        thread.sendSignal(signal);
        thread.clearQueue();
        return thread.context.cancelled;
    }
}
