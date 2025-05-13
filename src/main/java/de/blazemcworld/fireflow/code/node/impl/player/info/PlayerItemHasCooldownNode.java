package de.blazemcworld.fireflow.code.node.impl.player.info;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.ItemType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class PlayerItemHasCooldownNode extends Node {

    public PlayerItemHasCooldownNode() {
        super("player_item_has_cooldown", "Player Item Has Cooldown", 
            "Checks if a player item is on cooldown", Items.CLOCK);

        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<ItemStack> item = new Input<>("item", "Item", ItemType.INSTANCE);
        
        Output<Boolean> hasCooldown = new Output<>("has_cooldown", "Has Cooldown", ConditionType.INSTANCE);

        hasCooldown.valueFrom((ctx) -> {
            return player.getValue(ctx).tryGet(ctx, p -> {
                ItemStack stack = item.getValue(ctx);
                return p.getItemCooldownManager().isCoolingDown(stack);
            }, false);
        });
    }

    @Override
    public Node copy() {
        return new PlayerItemHasCooldownNode();
    }
}
