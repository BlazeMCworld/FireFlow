package de.blazemcworld.fireflow.code.node.impl.player.inventory;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ItemType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerHandItemsNode extends Node {
    public PlayerHandItemsNode() {
        super("player_hand_items", "Player Hand Items", "Gets the items in the player hands.", Items.IRON_SHOVEL);

        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Output<ItemStack> main = new Output<>("main_hand", "Main Hand", ItemType.INSTANCE);
        Output<ItemStack> off = new Output<>("off_hand", "Off Hand", ItemType.INSTANCE);

        main.valueFrom(ctx -> player.getValue(ctx).tryGet(ctx, ServerPlayerEntity::getMainHandStack, ItemType.INSTANCE.defaultValue()));
        off.valueFrom(ctx -> player.getValue(ctx).tryGet(ctx, ServerPlayerEntity::getOffHandStack, ItemType.INSTANCE.defaultValue()));
    }

    @Override
    public Node copy() {
        return new PlayerHandItemsNode();
    }
}