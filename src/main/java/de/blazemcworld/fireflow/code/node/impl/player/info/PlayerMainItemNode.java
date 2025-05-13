package de.blazemcworld.fireflow.code.node.impl.player.info;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ItemType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerMainItemNode extends Node {
    public PlayerMainItemNode() {
        super("player_main_item", "Player Main Item", "Gets the item in the player's main hand", Items.IRON_SHOVEL);

        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Output<ItemStack> item = new Output<>("item", "Item", ItemType.INSTANCE);

        item.valueFrom(ctx -> player.getValue(ctx).tryGet(ctx, ServerPlayerEntity::getMainHandStack, ItemType.INSTANCE.defaultValue()));
    }

    @Override
    public Node copy() {
        return new PlayerMainItemNode();
    }
}