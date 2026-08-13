package de.blazemcworld.fireflow.code.node.impl.event.action;

import de.blazemcworld.fireflow.code.CodeEvaluator;
import de.blazemcworld.fireflow.code.CodeThread;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;

public class OnPlayerStopFlyingNode extends Node {

    private final Output<Void> signal;
    private final Output<PlayerValue> player;

    public OnPlayerStopFlyingNode() {
        super("on_player_stop_fly", "On Player Stop Fly", "Emits a signal when a player stops flying (not gliding with an elytra).", Items.ANDESITE);

        signal = new Output<>("signal", "Signal", SignalType.INSTANCE);
        player = new Output<>("player", "Player", PlayerType.INSTANCE);
        player.valueFromScope();
    }

    @Override
    public Node copy() {
        return new OnPlayerStopFlyingNode();
    }

    public boolean onStopFlying(CodeEvaluator evaluator, ServerPlayer player, boolean cancel) {
        CodeThread thread = evaluator.newCodeThread();
        thread.context.cancelled = cancel;
        thread.setScopeValue(this.player, new PlayerValue(player));
        thread.sendSignal(signal);
        thread.clearQueue();
        return thread.context.cancelled;
    }
}
