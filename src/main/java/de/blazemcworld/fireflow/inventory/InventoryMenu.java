package de.blazemcworld.fireflow.inventory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class InventoryMenu extends ChestMenu {

    protected final ServerPlayer player;

    public InventoryMenu(int syncId, ServerPlayer player) {
        super(MenuType.GENERIC_9x3, syncId, player.getInventory(), new SimpleContainer(27), 3);
        this.player = player;
    }

    public void setItem(int slot, ItemStack stack) {
        getContainer().setItem(slot, stack);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player == this.player;
    }
}
