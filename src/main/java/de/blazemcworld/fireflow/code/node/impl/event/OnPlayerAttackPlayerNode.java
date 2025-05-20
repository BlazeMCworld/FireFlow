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

public class OnPlayerAttackPlayerNode extends Node {

    private final Output<Void> signal;
    private final Output<PlayerValue> attacker;
    private final Output<PlayerValue> victim;
    private final Output<Double> amount;

    public OnPlayerAttackPlayerNode() {
        super("on_player_attack_player", "On Player Attack Player", "Emits a signal when a player attacks another player.", Items.WOODEN_SWORD);

        signal = new Output<>("signal", "Signal", SignalType.INSTANCE);
        attacker = new Output<>("attacker", "Attacker", PlayerType.INSTANCE);
        victim = new Output<>("victim", "Victim", PlayerType.INSTANCE);
        amount = new Output<>("amount", "Amount", NumberType.INSTANCE);
        attacker.valueFromScope();
        victim.valueFromScope();
        amount.valueFromScope();
    }

    public void onPlayerAttackPlayer(CodeEvaluator codeEvaluator, ServerPlayerEntity attacker, ServerPlayerEntity victim, float damage, CodeThread.EventContext ctx) {
        CodeThread thread = codeEvaluator.newCodeThread();
        thread.context = ctx;
        thread.setScopeValue(this.attacker, new PlayerValue(attacker));
        thread.setScopeValue(this.victim, new PlayerValue(victim));
        thread.setScopeValue(this.amount, (double) damage);
        thread.sendSignal(signal);
        thread.clearQueue();
    }

    @Override
    public Node copy() {
        return new OnPlayerAttackPlayerNode();
    }

}
