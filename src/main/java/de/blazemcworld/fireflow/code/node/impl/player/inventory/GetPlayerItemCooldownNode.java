package de.blazemcworld.fireflow.code.node.impl.player.inventory;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ItemType;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class GetPlayerItemCooldownNode extends Node {

    public GetPlayerItemCooldownNode() {
        super("get_player_item_cooldown", "Get Player Item Cooldown", 
            "Gets the remaining cooldown percentage (0-1) for a specific item for a player", Items.POPPED_CHORUS_FRUIT);

        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<ItemStack> item = new Input<>("item", "Item", ItemType.INSTANCE);
        
        Output<Double> cooldown = new Output<>("cooldown", "Cooldown", NumberType.INSTANCE);

        cooldown.valueFrom((ctx) -> {
            return player.getValue(ctx).tryGet(ctx, p -> {
                ItemStack stack = item.getValue(ctx);
                return (double) p.getItemCooldownManager().getCooldownProgress(stack, 0);
            }, 0.0);
        });
    }

    @Override
    public Node copy() {
        return new GetPlayerItemCooldownNode();
    }
}
