package de.blazemcworld.fireflow.code.node.impl.entity;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.EntityType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.EntityValue;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;

public class RemoveEntityNode extends Node {

    public RemoveEntityNode() {
        super("remove_entity", "Remove Entity", "Despawns an entity", Items.TNT_MINECART);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<EntityValue> entity = new Input<>("entity", "Entity", EntityType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            entity.getValue(ctx).use(ctx, Entity::discard);
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new RemoveEntityNode();
    }
}
