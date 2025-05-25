package de.blazemcworld.fireflow.inventory;

import de.blazemcworld.fireflow.code.type.TextType;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceInfo;
import de.blazemcworld.fireflow.space.SpaceManager;
import de.blazemcworld.fireflow.util.ModeManager;
import de.blazemcworld.fireflow.util.ProfileApi;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class ActiveSpacesMenu extends InventoryMenu {

    private final List<SpaceInfo> infos = new ArrayList<>();

    public ActiveSpacesMenu(int syncId, ServerPlayerEntity player) {
        super(syncId, player);

        for (Space s : SpaceManager.activeSpaces()) {
            infos.add(s.info);
            if (infos.size() >= 26) break;
        }

        for (int i = 0; i < infos.size(); i++) {
            SpaceInfo info = infos.get(i);
            int players = 0;
            Space s = SpaceManager.getIfLoaded(info);
            if (s != null) players = s.playWorld.getPlayers().size();

            ItemStack item = new ItemStack(info.icon);
            item.set(DataComponentTypes.ITEM_NAME, TextType.INSTANCE.parseInset(info.name));
            Style loreStyle = Style.EMPTY.withItalic(false).withColor(Formatting.GRAY);
            item.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                    Text.literal("by " + ProfileApi.displayName(info.owner)).setStyle(loreStyle),
                    Text.literal("Players: " + players).setStyle(loreStyle),
                    Text.literal("ID: " + info.id).setStyle(loreStyle)
            )));

            setStack(i, item);
        }
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (this.player != player) return;

        if (slotIndex >= 0 && slotIndex < infos.size()) {
            SpaceInfo info = infos.get(slotIndex);
            ModeManager.move(this.player, ModeManager.Mode.PLAY, SpaceManager.getOrLoadSpace(info));
            return;
        }
    }

    public static void open(ServerPlayerEntity player) {
        player.openHandledScreen(new NamedScreenHandlerFactory() {
            @Override
            public Text getDisplayName() {
                return Text.literal("Active Spaces");
            }

            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new ActiveSpacesMenu(syncId, (ServerPlayerEntity) player);
            }
        });
    }
}
