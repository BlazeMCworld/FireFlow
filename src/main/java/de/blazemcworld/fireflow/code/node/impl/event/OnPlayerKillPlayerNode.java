package de.blazemcworld.fireflow.code.node.impl.event;

import de.blazemcworld.fireflow.code.CodeEvaluator;
import de.blazemcworld.fireflow.code.CodeThread;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

public class OnPlayerKillPlayerNode extends Node {

    private final Output<Void> signal;
    private final Output<PlayerValue> attacker;
    private final Output<PlayerValue> victim;
    private final Output<Double> amount;

    public OnPlayerKillPlayerNode() {
        super("on_player_kill_player", "On Player Kill Player", "Emits a signal when a player kills another player.", Items.NETHERITE_SWORD);

        signal = new Output<>("signal", "Signal", SignalType.INSTANCE);
        attacker = new Output<>("attacker", "Attacker", PlayerType.INSTANCE);
        victim = new Output<>("victim", "Victim", PlayerType.INSTANCE);
        amount = new Output<>("amount", "Amount", NumberType.INSTANCE);
        attacker.valueFromScope();
        victim.valueFromScope();
        amount.valueFromScope();
    }

    public boolean onPlayerKillPlayer(CodeEvaluator codeEvaluator, ServerPlayerEntity attacker, ServerPlayerEntity victim, float damage) {
        CodeThread thread = codeEvaluator.newCodeThread();
        thread.setScopeValue(this.attacker, new PlayerValue(attacker));
        thread.setScopeValue(this.victim, new PlayerValue(victim));
        thread.setScopeValue(this.amount, (double) damage);
        thread.sendSignal(signal);
        thread.clearQueue();
        return thread.eventCancelled;
    }

    @Override
    public Node copy() {
        return new OnPlayerKillPlayerNode();
    }

}
