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
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

public class OnEntityKillPlayerNode extends Node {

    private final Output<Void> signal;
    private final Output<EntityValue> attacker;
    private final Output<PlayerValue> victim;
    private final Output<Double> amount;

    public OnEntityKillPlayerNode() {
        super("on_entity_kill_player", "On Entity Kill Player", "Emits a signal when an entity kills a player.", Items.BOW);

        signal = new Output<>("signal", "Signal", SignalType.INSTANCE);
        attacker = new Output<>("attacker", "Attacker", EntityType.INSTANCE);
        victim = new Output<>("victim", "Victim", PlayerType.INSTANCE);
        amount = new Output<>("amount", "Amount", NumberType.INSTANCE);
        attacker.valueFromScope();
        victim.valueFromScope();
        amount.valueFromScope();
    }

    public boolean onEntityKillPlayer(CodeEvaluator codeEvaluator, Entity attacker, ServerPlayerEntity victim, float damage) {
        CodeThread thread = codeEvaluator.newCodeThread();
        thread.setScopeValue(this.attacker, new EntityValue(attacker));
        thread.setScopeValue(this.victim, new PlayerValue(victim));
        thread.setScopeValue(this.amount, (double) damage);
        thread.sendSignal(signal);
        thread.clearQueue();
        return thread.eventCancelled;
    }

    @Override
    public Node copy() {
        return new OnEntityKillPlayerNode();
    }

}
