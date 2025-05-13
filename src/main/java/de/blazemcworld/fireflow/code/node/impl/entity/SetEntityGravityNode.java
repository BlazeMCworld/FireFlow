package de.blazemcworld.fireflow.code.node.impl.entity;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.EntityType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.EntityValue;
import net.minecraft.item.Items;

public class SetEntityGravityNode extends Node {

    public SetEntityGravityNode() {
        super("set_entity_gravity", "Set Entity Gravity", "Enables or disables gravity for an entity", Items.SHULKER_SHELL);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<EntityValue> entity = new Input<>("entity", "Entity", EntityType.INSTANCE);
        Input<Boolean> enable = new Input<>("enable", "Enable", ConditionType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            entity.getValue(ctx).use(ctx, e -> e.setNoGravity(!enable.getValue(ctx)));
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new SetEntityGravityNode();
    }
}
