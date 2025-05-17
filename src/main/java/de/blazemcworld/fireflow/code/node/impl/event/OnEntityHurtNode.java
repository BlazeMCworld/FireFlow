package de.blazemcworld.fireflow.code.node.impl.event;

import de.blazemcworld.fireflow.code.CodeEvaluator;
import de.blazemcworld.fireflow.code.CodeThread;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.EntityType;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.value.EntityValue;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;

public class OnEntityHurtNode extends Node {

    private final Output<Void> signal;
    private final Output<EntityValue> entity;
    private final Output<Double> amount;
    private final Output<String> type;

    public OnEntityHurtNode() {
        super("on_entity_hurt", "On Entity Hurt", "Emits a signal when an entity is about to take damage.", Items.REDSTONE_ORE);

        signal = new Output<>("signal", "Signal", SignalType.INSTANCE);
        entity = new Output<>("entity", "entity", EntityType.INSTANCE);
        amount = new Output<>("amount", "Damage Amount", NumberType.INSTANCE);
        type = new Output<>("type", "Damage Type", StringType.INSTANCE);
        entity.valueFromScope();
        amount.valueFromScope();
        type.valueFromScope();
    }

    public boolean onEntityHurt(CodeEvaluator codeEvaluator, LivingEntity entity, float damage, String type, boolean cancel) {
        CodeThread thread = codeEvaluator.newCodeThread();
        thread.eventCancelled = cancel;
        thread.setScopeValue(this.entity, new EntityValue(entity));
        thread.setScopeValue(this.amount, (double) damage);
        thread.setScopeValue(this.type, type);
        thread.sendSignal(signal);
        thread.clearQueue();
        return thread.eventCancelled;
    }

    @Override
    public Node copy() {
        return new OnEntityHurtNode();
    }
}

