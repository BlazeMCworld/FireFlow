package de.blazemcworld.fireflow.code.node.impl.entity;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.EntityType;
import de.blazemcworld.fireflow.code.type.ListType;
import de.blazemcworld.fireflow.code.value.EntityValue;
import de.blazemcworld.fireflow.code.value.ListValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

public class EntityListNode extends Node {

    public EntityListNode() {
        super("entity_list", "Entity List", "Gets a list of all entities currently in the space, players are excluded", Items.LIGHT_BLUE_DYE);

        Output<ListValue<EntityValue>> entities = new Output<>("entities", "Entities", ListType.of(EntityType.INSTANCE));

        entities.valueFrom((ctx) -> {
            List<EntityValue> out = new ArrayList<>();
            for (Entity entity : ctx.evaluator.world.iterateEntities()) {
                if (entity instanceof PlayerEntity) continue;
                out.add(new EntityValue(entity));
            }
            return new ListValue<>(EntityType.INSTANCE, out);
        });
    }

    @Override
    public Node copy() {
        return new EntityListNode();
    }
}
