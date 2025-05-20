package de.blazemcworld.fireflow.code.node.impl.event;

import de.blazemcworld.fireflow.code.CodeEvaluator;
import de.blazemcworld.fireflow.code.CodeThread;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

public class OnPlayerDropItemNode extends Node {

    private final Output<Void> signal;
    private final Output<PlayerValue> player;

    public OnPlayerDropItemNode() {
        super("on_player_drop_item", "On Player Drop Item", "Called when a player drops an item.", Items.DROPPER);

        signal = new Output<>("signal", "Signal", SignalType.INSTANCE);
        player = new Output<>("player", "Player", PlayerType.INSTANCE);
        player.valueFromScope();
    }

    @Override
    public Node copy() {
        return new OnPlayerDropItemNode();
    }

    public boolean onDropItem(CodeEvaluator codeEvaluator, ServerPlayerEntity player, boolean cancel) {
        CodeThread thread = codeEvaluator.newCodeThread();
        thread.context.cancelled = cancel;
        thread.setScopeValue(this.player, new PlayerValue(player));
        thread.sendSignal(signal);
        thread.clearQueue();
        return thread.context.cancelled;
    }
}
