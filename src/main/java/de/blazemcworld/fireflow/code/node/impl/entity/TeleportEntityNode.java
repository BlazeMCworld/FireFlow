package de.blazemcworld.fireflow.code.node.impl.entity;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.EntityType;
import de.blazemcworld.fireflow.code.type.PositionType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.EntityValue;
import de.blazemcworld.fireflow.code.value.Position;
import net.minecraft.item.Items;

import java.util.Set;

public class TeleportEntityNode extends Node {

    public TeleportEntityNode() {
        super("teleport_entity", "Teleport Entity", "Teleports an entity to a position", Items.ENDER_PEARL);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<EntityValue> entity = new Input<>("entity", "Entity", EntityType.INSTANCE);
        Input<Position> position = new Input<>("position", "Position", PositionType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            entity.getValue(ctx).use(ctx, e -> {
                Position pos = position.getValue(ctx);
                e.teleport(ctx.evaluator.world, pos.xyz().x, pos.xyz().y, pos.xyz().z, Set.of(), pos.yaw(), pos.pitch(), false);
            });
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new TeleportEntityNode();
    }
}