package de.blazemcworld.fireflow.code.node.impl.player.gameplay;

import com.mojang.serialization.DataResult;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.option.EffectOptions;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class TakePlayerEffectNode extends Node {

    public TakePlayerEffectNode() {
        super("take_player_effect", "Take Player Effect", "Takes an effect from the player", Items.LINGERING_POTION);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<String> effect = new Input<>("effect", "Effect", StringType.INSTANCE).options(EffectOptions.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            player.getValue(ctx).tryUse(ctx, p -> {
                DataResult<Identifier> id = Identifier.validate(effect.getValue(ctx));
                if (id.isError()) return;
                Optional<RegistryEntry.Reference<StatusEffect>> effectEntry = Registries.STATUS_EFFECT.getEntry(id.getOrThrow());
                if (effectEntry.isEmpty()) return;
                p.removeStatusEffect(effectEntry.get());
            });
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new TakePlayerEffectNode();
    }

}
