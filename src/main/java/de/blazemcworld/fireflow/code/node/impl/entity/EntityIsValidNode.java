package de.blazemcworld.fireflow.code.node.impl.entity;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.EntityType;
import de.blazemcworld.fireflow.code.value.EntityValue;
import net.minecraft.item.Items;

public class EntityIsValidNode extends Node {

    public EntityIsValidNode() {
        super("entity_is_valid", "Entity Is Valid", "Checks if the entity can be found, usually the case until the entity despawns.", Items.OAK_SAPLING);

        Input<EntityValue> entity = new Input<>("entity", "Entity", EntityType.INSTANCE);
        Output<Boolean> valid = new Output<>("valid", "Valid", ConditionType.INSTANCE);

        valid.valueFrom(ctx -> entity.getValue(ctx).apply(ctx, e -> true, false));
    }

    @Override
    public Node copy() {
        return new EntityIsValidNode();
    }
}
