package de.blazemcworld.fireflow.code.node.impl.event.meta;

import de.blazemcworld.fireflow.code.CodeEvaluator;
import de.blazemcworld.fireflow.code.CodeThread;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

public class OnPlayerChatNode extends Node {
    
    private final Output<Void> signal;
    private final Output<PlayerValue> player;
    private final Output<String> message;

    public OnPlayerChatNode() {
        super("on_player_chat", "On Player Chat", "Emits a signal when a player sends a chat message.", Items.NAME_TAG);

        signal = new Output<>("signal", "Signal", SignalType.INSTANCE);
        player = new Output<>("player", "Player", PlayerType.INSTANCE);
        message = new Output<>("message", "Message", StringType.INSTANCE);
        player.valueFromScope();
        message.valueFromScope();

    }

    @Override
    public Node copy() {
        return new OnPlayerChatNode();
    }

    public boolean onChat(CodeEvaluator evaluator, ServerPlayerEntity player, String message, boolean cancel) {
        CodeThread thread = evaluator.newCodeThread();
        thread.context.cancelled = cancel;
        thread.setScopeValue(this.player, new PlayerValue(player));
        thread.setScopeValue(this.message, message);
        thread.sendSignal(signal);
        thread.clearQueue();
        return thread.context.cancelled;
    }
}
