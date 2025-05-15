package de.blazemcworld.fireflow.code.node.impl.player.info;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ItemType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerOffhandItemNode extends Node {
    public PlayerOffhandItemNode() {
        super("player_offhand_item", "Player Offhand Item", "Gets the item in the player's offhand", Items.SHIELD);

        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Output<ItemStack> item = new Output<>("item", "Item", ItemType.INSTANCE);

        item.valueFrom(ctx -> player.getValue(ctx).tryGet(ctx, ServerPlayerEntity::getOffHandStack, ItemType.INSTANCE.defaultValue()));
    }

    @Override
    public Node copy() {
        return new PlayerOffhandItemNode();
    }
}