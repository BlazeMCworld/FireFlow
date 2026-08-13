package de.blazemcworld.fireflow.code.node.impl.player.gameplay;

import com.mojang.serialization.DataResult;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.option.EffectOptions;
import de.blazemcworld.fireflow.code.type.*;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Items;

import java.util.Optional;

public class GivePlayerEffectNode extends Node {

    public GivePlayerEffectNode() {
        super("give_player_effect", "Give Player Effect", "Gives the player a potion effect.", Items.SPLASH_POTION);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<String> effect = new Input<>("effect", "Effect", StringType.INSTANCE).options(EffectOptions.INSTANCE);
        Input<Double> duration = new Input<>("duration", "Duration", NumberType.INSTANCE);
        Input<Double> amplifier = new Input<>("amplifier", "Amplifier", NumberType.INSTANCE);
        Input<Boolean> isAmbient = new Input<>("ambient", "Ambient", ConditionType.INSTANCE);
        Input<Boolean> showParticles = new Input<>("show_particles", "Show Particles", ConditionType.INSTANCE);
        Input<Boolean> showIcon = new Input<>("show_icon", "Show Icon", ConditionType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            player.getValue(ctx).tryUse(ctx, p -> {
                DataResult<Identifier> id = Identifier.read(effect.getValue(ctx));
                if (id.isError()) return;
                Optional<Holder.Reference<MobEffect>> effectEntry = BuiltInRegistries.MOB_EFFECT.get(id.getOrThrow());
                if (effectEntry.isEmpty()) return;
                p.addEffect(new MobEffectInstance(
                        effectEntry.get(),
                        duration.getValue(ctx).intValue(), amplifier.getValue(ctx).intValue(),
                        isAmbient.getValue(ctx), showParticles.getValue(ctx), showIcon.getValue(ctx)
                ));
            });
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new GivePlayerEffectNode();
    }
}
