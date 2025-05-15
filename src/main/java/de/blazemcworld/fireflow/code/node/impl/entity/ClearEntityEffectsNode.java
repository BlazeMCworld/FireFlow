package de.blazemcworld.fireflow.code.node.impl.entity;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.EntityType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.EntityValue;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;

public class ClearEntityEffectsNode extends Node {

    public ClearEntityEffectsNode() {

        super("clear_player_effects", "Clear Entity Effects", "Removes all potion effects from an entity.", Items.MILK_BUCKET);
        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);
        Input<EntityValue> entity = new Input<>("entity", "Entity", EntityType.INSTANCE);

        signal.onSignal((ctx) -> {
            ctx.sendSignal(next);
            entity.getValue(ctx).use(ctx, e -> {
                if (e instanceof LivingEntity living) {
                    living.clearStatusEffects();
                }
            });
        });
    }

    @Override
    public Node copy() {
        return new ClearEntityEffectsNode();
    }
}