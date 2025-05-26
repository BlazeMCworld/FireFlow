package de.blazemcworld.fireflow.code.node.impl.event.combat.entity;

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

public class OnEntityDeathNode extends Node {

    private final Output<Void> signal;
    private final Output<EntityValue> entity;
    private final Output<Double> amount;
    private final Output<String> type;

    public OnEntityDeathNode() {
        super("on_entity_death", "On Entity Death", "Emits a signal when an entity is about to die.", Items.BONE);

        signal = new Output<>("signal", "Signal", SignalType.INSTANCE);
        entity = new Output<>("entity", "Entity", EntityType.INSTANCE);
        amount = new Output<>("amount", "Damage Amount", NumberType.INSTANCE);
        type = new Output<>("damage", "Damage Type", StringType.INSTANCE);
        entity.valueFromScope();
        amount.valueFromScope();
        type.valueFromScope();
    }

    @Override
    public Node copy() {
        return new OnEntityDeathNode();
    }

    public boolean onEntityDeath(CodeEvaluator codeEvaluator, LivingEntity target, float damage, String type, boolean cancel) {
        CodeThread thread = codeEvaluator.newCodeThread();
        thread.context.cancelled = cancel;
        thread.setScopeValue(this.entity, new EntityValue(target));
        thread.setScopeValue(this.amount, (double) damage);
        thread.setScopeValue(this.type, type);
        thread.sendSignal(signal);
        thread.clearQueue();
        return thread.context.cancelled;
    }
}
