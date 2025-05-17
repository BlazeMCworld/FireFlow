package de.blazemcworld.fireflow.code.node.impl.entity;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.EntityType;
import de.blazemcworld.fireflow.code.type.PositionType;
import de.blazemcworld.fireflow.code.value.EntityValue;
import de.blazemcworld.fireflow.code.value.Position;
import net.minecraft.item.Items;

public class EntityPositionNode extends Node {

    public EntityPositionNode() {
        super("entity_position", "Entity Position", "Gets the current coordinates of the entity.", Items.RECOVERY_COMPASS);

        Input<EntityValue> entity = new Input<>("entity", "Entity", EntityType.INSTANCE);
        Output<Position> position = new Output<>("position", "Position", PositionType.INSTANCE);

        position.valueFrom((ctx) -> entity.getValue(ctx)
                .apply(ctx, p -> new Position(p.getPos(), p.getPitch(), p.getYaw()), PositionType.INSTANCE.defaultValue()));
    }

    @Override
    public Node copy() {
        return new EntityPositionNode();
    }

}
