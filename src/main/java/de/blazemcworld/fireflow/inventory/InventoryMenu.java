package de.blazemcworld.fireflow.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;

public class InventoryMenu extends GenericContainerScreenHandler {

    protected final ServerPlayerEntity player;

    public InventoryMenu(int syncId, ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X3, syncId, player.getInventory(), new SimpleInventory(27), 3);
        this.player = player;
    }

    public void setStack(int slot, ItemStack stack) {
        getInventory().setStack(slot, stack);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return player == this.player;
    }
}
