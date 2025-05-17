package de.blazemcworld.fireflow.code.node.impl.entity;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.EntityType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.value.EntityValue;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

public class EntityTypeNode extends Node {

    public EntityTypeNode() {
        super("entity_type", "Entity Type", "Returns the type, given an entity, or 'invalid' if the entity could not be found.", Items.SPYGLASS);

        Input<EntityValue> entity = new Input<>("entity", "Entity", EntityType.INSTANCE);
        Output<String> type = new Output<>("type", "Type", StringType.INSTANCE);

        type.valueFrom((ctx) -> entity.getValue(ctx).apply(ctx, e -> Registries.ENTITY_TYPE.getId(e.getType()).getPath(), "invalid"));
    }

    @Override
    public Node copy() {
        return new EntityTypeNode();
    }
}
