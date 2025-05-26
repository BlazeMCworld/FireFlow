package de.blazemcworld.fireflow.code.node.impl.event.combat.entity;

import de.blazemcworld.fireflow.code.CodeEvaluator;
import de.blazemcworld.fireflow.code.CodeThread;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.EntityType;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.EntityValue;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;

public class OnEntityAttackEntityNode extends Node {

    private final Output<Void> signal;
    private final Output<EntityValue> attacker;
    private final Output<EntityValue> victim;
    private final Output<Double> amount;

    public OnEntityAttackEntityNode() {
        super("on_entity_attack_entity", "On Entity Attack Entity", "Emits a signal when an entity attacks an entity.", Items.GOLDEN_SHOVEL);

        signal = new Output<>("signal", "Signal", SignalType.INSTANCE);
        attacker = new Output<>("attacker", "Attacker", EntityType.INSTANCE);
        victim = new Output<>("victim", "Victim", EntityType.INSTANCE);
        amount = new Output<>("amount", "Amount", NumberType.INSTANCE);
        attacker.valueFromScope();
        victim.valueFromScope();
        amount.valueFromScope();
    }

    public void onEntityAttackEntity(CodeEvaluator codeEvaluator, Entity attacker, Entity victim, float damage, CodeThread.EventContext ctx) {
        CodeThread thread = codeEvaluator.newCodeThread();
        thread.context = ctx;
        thread.setScopeValue(this.attacker, new EntityValue(attacker));
        thread.setScopeValue(this.victim, new EntityValue(victim));
        thread.setScopeValue(this.amount, (double) damage);
        thread.sendSignal(signal);
        thread.clearQueue();
    }

    @Override
    public Node copy() {
        return new OnEntityAttackEntityNode();
    }

}