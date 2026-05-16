package de.blazemcworld.fireflow.code.node.impl.event.world;

import de.blazemcworld.fireflow.code.CodeEvaluator;
import de.blazemcworld.fireflow.code.CodeThread;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

public class OnPlayerRespawnNode extends Node {
    private final Output<Void> signal;
    private final Output<PlayerValue> player;

    public OnPlayerRespawnNode() {
        super("on_player_respawn", "On Player Respawn", "Emits a signal when a player respawns.", Items.OAK_SAPLING);

        signal = new Output<>("signal", "Signal", SignalType.INSTANCE);
        player = new Output<>("player", "Player", PlayerType.INSTANCE);
        player.valueFromScope();
    }

    @Override
    public Node copy() {
        return new OnPlayerRespawnNode();
    }

    public void onRespawn(CodeEvaluator evaluator, ServerPlayerEntity p) {
        CodeThread thread = evaluator.newCodeThread();
        thread.setScopeValue(player, new PlayerValue(p));
        thread.sendSignal(signal);
        thread.clearQueue();
    }
}
