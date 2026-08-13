package de.blazemcworld.fireflow.code.node.impl.entity;

import com.mojang.serialization.DataResult;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.EntityType;
import de.blazemcworld.fireflow.code.type.PositionType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.value.EntityValue;
import de.blazemcworld.fireflow.code.value.Position;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Items;

import java.util.Optional;

public class SpawnEntityNode extends Node {

    public SpawnEntityNode() {
        super("spawn_entity", "Spawn Entity", "Spawns a new entity at the given coordinates.", Items.DARK_OAK_SAPLING);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<Position> position = new Input<>("position", "Position", PositionType.INSTANCE);
        Input<String> type = new Input<>("type", "Type", StringType.INSTANCE);

        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);
        Output<EntityValue> entity = new Output<>("entity", "Entity", EntityType.INSTANCE);
        entity.valueFromScope();

        signal.onSignal((ctx) -> {
            DataResult<Identifier> id = Identifier.read(type.getValue(ctx));
            if (id.isError()) {
                ctx.sendSignal(next);
                return;
            }
            Optional<Holder.Reference<net.minecraft.world.entity.EntityType<?>>> entityValue = BuiltInRegistries.ENTITY_TYPE.get(id.getOrThrow());

            if (entityValue.isPresent()) {
                Entity spawned = entityValue.get().value().create(ctx.evaluator.level, EntitySpawnReason.COMMAND);
                ctx.setScopeValue(entity, new EntityValue(spawned));
                Position pos = position.getValue(ctx);
                if (spawned != null) {
                    spawned.setPos(pos.xyz());
                    spawned.setXRot(pos.pitch());
                    spawned.setYRot(pos.yaw());
                    ctx.evaluator.level.addFreshEntity(spawned);
                }
            }

            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new SpawnEntityNode();
    }
}
