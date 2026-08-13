package de.blazemcworld.fireflow.code.node.impl.entity;

import com.mojang.serialization.DataResult;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.option.EffectOptions;
import de.blazemcworld.fireflow.code.type.EntityType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.value.EntityValue;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;

import java.util.Optional;

public class TakeEntityEffectNode extends Node {

    public TakeEntityEffectNode() {
        super("take_entity_effect", "Take Entity Effect", "Takes an effect from the entity", Items.LINGERING_POTION);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<EntityValue> entity = new Input<>("entity", "Entity", EntityType.INSTANCE);
        Input<String> effect = new Input<>("effect", "Effect", StringType.INSTANCE).options(EffectOptions.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            entity.getValue(ctx).use(ctx, e -> {
                if (!(e instanceof LivingEntity living)) return;
                DataResult<Identifier> id = Identifier.read(effect.getValue(ctx));
                if (id.isError()) return;
                Optional<Holder.Reference<MobEffect>> effectEntry = BuiltInRegistries.MOB_EFFECT.get(id.getOrThrow());
                if (effectEntry.isEmpty()) return;
                living.removeEffect(effectEntry.get());
            });
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new TakeEntityEffectNode();
    }

}
