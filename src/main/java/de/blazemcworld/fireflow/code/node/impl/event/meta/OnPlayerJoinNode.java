package de.blazemcworld.fireflow.code.node.impl.event.meta;

import de.blazemcworld.fireflow.code.CodeEvaluator;
import de.blazemcworld.fireflow.code.CodeThread;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

public class OnPlayerJoinNode extends Node {

    private final Output<Void> signal;
    private final Output<PlayerValue> player;

    public OnPlayerJoinNode() {
        super("on_player_join", "On Player Join", "Emits a signal when a player joins.", Items.OAK_DOOR);

        signal = new Output<>("signal", "Signal", SignalType.INSTANCE);
        player = new Output<>("player", "Player", PlayerType.INSTANCE);
        player.valueFromScope();
    }

    @Override
    public Node copy() {
        return new OnPlayerJoinNode();
    }

    public void onJoin(CodeEvaluator evaluator, ServerPlayerEntity p) {
        CodeThread thread = evaluator.newCodeThread();
        thread.setScopeValue(player, new PlayerValue(p));
        thread.sendSignal(signal);
        thread.clearQueue();
    }
}
