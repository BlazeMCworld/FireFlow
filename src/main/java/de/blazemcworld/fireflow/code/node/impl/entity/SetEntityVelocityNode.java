package de.blazemcworld.fireflow.code.node.impl.entity;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.EntityType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.VectorType;
import de.blazemcworld.fireflow.code.value.EntityValue;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class SetEntityVelocityNode extends Node {

    public SetEntityVelocityNode() {
        super("set_entity_velocity", "Set Entity Velocity", "Sets the current motion of an entity.", Items.ARROW);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<EntityValue> entity = new Input<>("entity", "Entity", EntityType.INSTANCE);
        Input<Vec3> velocity = new Input<>("velocity", "Velocity", VectorType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            entity.getValue(ctx).use(ctx, e -> {
                e.setDeltaMovement(velocity.getValue(ctx));
            });
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new SetEntityVelocityNode();
    }
}
