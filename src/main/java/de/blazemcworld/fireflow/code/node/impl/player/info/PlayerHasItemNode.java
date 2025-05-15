package de.blazemcworld.fireflow.code.node.impl.player.info;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.ItemType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class PlayerHasItemNode extends Node {
    public PlayerHasItemNode() {
        super("player_has_item", "Player has Item", "Checks if the player has a specific item in their inventory", Items.CHEST);

        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<ItemStack> item = new Input<>("item", "Item", ItemType.INSTANCE);
        Output<Boolean> found = new Output<>("found", "Found", ConditionType.INSTANCE);

        found.valueFrom((ctx) -> player.getValue(ctx).tryGet(ctx, p -> {
            ItemStack val = item.getValue(ctx);
            if (p.getInventory().contains(val)) return true;
            for (ItemStack other : p.playerScreenHandler.getCraftingInput()) {
                if (ItemStack.areItemsAndComponentsEqual(val, other)) return true;
            }
            return false;
        }, false));
    }

    @Override
    public PlayerHasItemNode copy() {
        return new PlayerHasItemNode();
    }
}