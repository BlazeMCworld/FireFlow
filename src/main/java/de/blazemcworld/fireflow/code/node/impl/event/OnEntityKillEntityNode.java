package de.blazemcworld.fireflow.code.node.impl.event;

import de.blazemcworld.fireflow.code.CodeEvaluator;
import de.blazemcworld.fireflow.code.CodeThread;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.EntityType;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.EntityValue;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;

public class OnEntityKillEntityNode extends Node {

    private final Output<Void> signal;
    private final Output<EntityValue> attacker;
    private final Output<EntityValue> victim;
    private final Output<Double> amount;

    public OnEntityKillEntityNode() {
        super("on_entity_kill_entity", "On Entity Kill Entity", "Emits a signal when an entity kills an entity.", Items.CROSSBOW);

        signal = new Output<>("signal", "Signal", SignalType.INSTANCE);
        attacker = new Output<>("attacker", "Attacker", EntityType.INSTANCE);
        victim = new Output<>("victim", "Victim", EntityType.INSTANCE);
        amount = new Output<>("amount", "Amount", NumberType.INSTANCE);
        attacker.valueFromScope();
        victim.valueFromScope();
        amount.valueFromScope();
    }

    public boolean onEntityKillEntity(CodeEvaluator codeEvaluator, Entity attacker, Entity victim, float damage, boolean cancel) {
        CodeThread thread = codeEvaluator.newCodeThread();
        thread.eventCancelled = cancel;
        thread.setScopeValue(this.attacker, new EntityValue(attacker));
        thread.setScopeValue(this.victim, new EntityValue(victim));
        thread.setScopeValue(this.amount, (double) damage);
        thread.sendSignal(signal);
        thread.clearQueue();
        return thread.eventCancelled;
    }

    @Override
    public Node copy() {
        return new OnEntityKillEntityNode();
    }

}