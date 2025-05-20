package de.blazemcworld.fireflow.code.node.impl.event;

import de.blazemcworld.fireflow.code.CodeEvaluator;
import de.blazemcworld.fireflow.code.CodeThread;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.EntityType;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.EntityValue;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

public class OnPlayerKillEntityNode extends Node {

    private final Output<Void> signal;
    private final Output<PlayerValue> attacker;
    private final Output<EntityValue> victim;
    private final Output<Double> amount;

    public OnPlayerKillEntityNode() {
        super("on_player_kill_entity", "On Player Kill Entity", "Emits a signal when a player kills an entity.", Items.DIAMOND_SWORD);

        signal = new Output<>("signal", "Signal", SignalType.INSTANCE);
        attacker = new Output<>("attacker", "Attacker", PlayerType.INSTANCE);
        victim = new Output<>("victim", "Victim", EntityType.INSTANCE);
        amount = new Output<>("amount", "Amount", NumberType.INSTANCE);
        attacker.valueFromScope();
        victim.valueFromScope();
        amount.valueFromScope();
    }

    public boolean onPlayerKillEntity(CodeEvaluator codeEvaluator, ServerPlayerEntity attacker, LivingEntity victim, float damage, boolean cancel) {
        CodeThread thread = codeEvaluator.newCodeThread();
        thread.context.cancelled = cancel;
        thread.setScopeValue(this.attacker, new PlayerValue(attacker));
        thread.setScopeValue(this.victim, new EntityValue(victim));
        thread.setScopeValue(this.amount, (double) damage);
        thread.sendSignal(signal);
        thread.clearQueue();
        return thread.context.cancelled;
    }

    @Override
    public Node copy() {
        return new OnPlayerKillEntityNode();
    }

}