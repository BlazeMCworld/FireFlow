package de.blazemcworld.fireflow.code.node.impl.event.player;

import de.blazemcworld.fireflow.code.CodeEvaluator;
import de.blazemcworld.fireflow.code.CodeThread;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.item.Material;

public class OnPlayerChatNode extends Node {
    
    private final Output<Void> signal;
    private final Output<PlayerValue> player;
    private final Output<String> message;

    public OnPlayerChatNode() {
        super("on_player_chat", Material.NAME_TAG);

        signal = new Output<>("signal", SignalType.INSTANCE);
        player = new Output<>("player", PlayerType.INSTANCE);
        message = new Output<>("message", StringType.INSTANCE);
        player.valueFromScope();
        message.valueFromScope();

    }

    @Override
    public void init(CodeEvaluator evaluator) {
        evaluator.events.addListener(PlayerChatEvent.class, event -> {
            CodeThread thread = evaluator.newCodeThread(event);
            thread.setScopeValue(player, new PlayerValue(event.getPlayer()));
            thread.setScopeValue(message, event.getRawMessage());
            thread.sendSignal(signal);
            thread.clearQueue();
        });
    }

    @Override
    public Node copy() {
        return new OnPlayerChatNode();
    }

}
