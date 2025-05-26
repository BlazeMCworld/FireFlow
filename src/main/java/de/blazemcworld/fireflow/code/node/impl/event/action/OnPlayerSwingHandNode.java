package de.blazemcworld.fireflow.code.node.impl.event.action;

import de.blazemcworld.fireflow.code.CodeEvaluator;
import de.blazemcworld.fireflow.code.CodeThread;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

public class OnPlayerSwingHandNode extends Node {

    private final Output<Void> signal;
    private final Output<PlayerValue> player;
    private final Output<Boolean> isMainHand;

    public OnPlayerSwingHandNode() {
        super("on_player_swing_hand", "On Player Swing Hand", "Called when a player swings their hand, usually bound to left click.", Items.STONE_SWORD);

        signal = new Output<>("signal", "Signal", SignalType.INSTANCE);
        player = new Output<>("player", "Player", PlayerType.INSTANCE);
        isMainHand = new Output<>("is_main_hand", "Is Main Hand", ConditionType.INSTANCE);
        player.valueFromScope();
        isMainHand.valueFromScope();
    }

    @Override
    public Node copy() {
        return new OnPlayerSwingHandNode();
    }

    public boolean onSwingHand(CodeEvaluator codeEvaluator, ServerPlayerEntity player, boolean isMainHand, boolean cancel) {
        CodeThread thread = codeEvaluator.newCodeThread();
        thread.context.cancelled = cancel;
        thread.setScopeValue(this.player, new PlayerValue(player));
        thread.setScopeValue(this.isMainHand, isMainHand);
        thread.sendSignal(signal);
        thread.clearQueue();
        return thread.context.cancelled;
    }
}
