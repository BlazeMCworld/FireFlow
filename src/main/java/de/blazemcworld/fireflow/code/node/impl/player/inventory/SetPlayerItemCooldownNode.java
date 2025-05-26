package de.blazemcworld.fireflow.code.node.impl.player.inventory;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ItemType;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class SetPlayerItemCooldownNode extends Node {

    public SetPlayerItemCooldownNode() {
        super("set_player_item_cooldown", "Set Player Item Cooldown", "Sets a cooldown for a specific item for a player", Items.CLOCK);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<ItemStack> item = new Input<>("item", "Item", ItemType.INSTANCE);
        Input<Double> ticks = new Input<>("ticks", "Ticks", NumberType.INSTANCE);
        
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            player.getValue(ctx).tryUse(ctx, p -> {
                ItemStack cooldownItem = item.getValue(ctx);
                int cooldownTicks = ticks.getValue(ctx).intValue();
                p.getItemCooldownManager().set(cooldownItem, cooldownTicks);
            });
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new SetPlayerItemCooldownNode();
    }
}
