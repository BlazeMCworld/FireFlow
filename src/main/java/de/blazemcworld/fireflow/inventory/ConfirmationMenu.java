package de.blazemcworld.fireflow.inventory;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ConfirmationMenu extends InventoryMenu {

    private final Runnable confirm;
    private final Runnable cancel;
    private boolean isAnswered = false;

    private ConfirmationMenu(int syncId, ServerPlayerEntity player, String question, Runnable confirm, Runnable cancel) {
        super(syncId, player);
        this.confirm = confirm == null ? () -> {} : confirm;
        this.cancel = cancel == null ? () -> {} : cancel;

        ItemStack cancelBtn = new ItemStack(Items.REDSTONE_BLOCK);
        cancelBtn.set(DataComponentTypes.ITEM_NAME, Text.literal("Cancel").formatted(Formatting.RED));

        ItemStack confirmBtn = new ItemStack(Items.EMERALD_BLOCK);
        confirmBtn.set(DataComponentTypes.ITEM_NAME, Text.literal("Confirm").formatted(Formatting.GREEN));

        ItemStack questionStack = new ItemStack(Items.PAPER);
        questionStack.set(DataComponentTypes.ITEM_NAME, Text.literal(question).formatted(Formatting.WHITE));
        questionStack.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("Are you sure about this?").setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.GRAY)),
                Text.literal("If unsure, press cancel or close the inventory.").setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.GRAY))
        )));
        setStack(10, questionStack);

        setStack(11, cancelBtn);
        setStack(14, confirmBtn);
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (this.player != player) return;
        if (isAnswered) return;

        if (slotIndex == 11) {
            isAnswered = true;
            this.cancel.run();
            this.player.closeHandledScreen();
            return;
        }

        if (slotIndex == 14) {
            isAnswered = true;
            this.confirm.run();
            this.player.closeHandledScreen();
            return;
        }
    }

    @Override
    public void onClosed(PlayerEntity player) {
        if (isAnswered) return;
        isAnswered = true;
        this.cancel.run();
    }

    public static void open(ServerPlayerEntity player, String question, Runnable confirm, Runnable cancel) {
        player.openHandledScreen(new NamedScreenHandlerFactory() {
            @Override
            public Text getDisplayName() {
                return Text.literal("Active Spaces");
            }

            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new ConfirmationMenu(syncId, (ServerPlayerEntity) player, question, confirm, cancel);
            }
        });
    }

}
