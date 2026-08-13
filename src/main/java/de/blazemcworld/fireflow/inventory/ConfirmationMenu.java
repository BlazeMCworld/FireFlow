package de.blazemcworld.fireflow.inventory;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class ConfirmationMenu extends InventoryMenu {

    private final Runnable confirm;
    private final Runnable cancel;
    private boolean isAnswered = false;

    private ConfirmationMenu(int syncId, ServerPlayer player, String question, Runnable confirm, Runnable cancel) {
        super(syncId, player);
        this.confirm = confirm == null ? () -> {} : confirm;
        this.cancel = cancel == null ? () -> {} : cancel;

        ItemStack cancelBtn = new ItemStack(Items.REDSTONE_BLOCK);
        cancelBtn.set(DataComponents.ITEM_NAME, Component.literal("Cancel").withColor(TextColor.RED));

        ItemStack confirmBtn = new ItemStack(Items.EMERALD_BLOCK);
        confirmBtn.set(DataComponents.ITEM_NAME, Component.literal("Confirm").withColor(TextColor.GREEN));

        ItemStack questionStack = new ItemStack(Items.PAPER);
        questionStack.set(DataComponents.ITEM_NAME, Component.literal(question).withColor(TextColor.WHITE));
        questionStack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("Are you sure about this?").setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.GRAY)),
                Component.literal("If unsure, press cancel or close the inventory.").setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.GRAY))
        )));
        setItem(10, questionStack);

        setItem(11, cancelBtn);
        setItem(14, confirmBtn);
    }

    @Override
    public void clicked(int slotIndex, int button, @NonNull ContainerInput type, @NonNull Player player) {
        if (this.player != player) return;
        if (isAnswered) return;

        if (slotIndex == 11) {
            isAnswered = true;
            this.cancel.run();
            this.player.closeContainer();
            return;
        }

        if (slotIndex == 14) {
            isAnswered = true;
            this.confirm.run();
            this.player.closeContainer();
            return;
        }
    }

    @Override
    public void removed(@NonNull Player player) {
        if (isAnswered) return;
        isAnswered = true;
        this.cancel.run();
    }

    public static void open(ServerPlayer player, String question, Runnable confirm, Runnable cancel) {
        player.openMenu(new MenuProvider() {
            @Override
            public @NonNull Component getDisplayName() {
                return Component.literal("Active Spaces");
            }

            @Override
            public AbstractContainerMenu createMenu(int syncId, @NonNull Inventory inv, @NonNull Player player) {
                return new ConfirmationMenu(syncId, (ServerPlayer) player, question, confirm, cancel);
            }
        });
    }

}
