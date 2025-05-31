package de.blazemcworld.fireflow.inventory;

import de.blazemcworld.fireflow.code.type.TextType;
import de.blazemcworld.fireflow.messages.ColourPalette;
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
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashSet;
import java.util.List;

public class MySpacesMenu extends InventoryMenu {

    private List<SpaceInfo> infos;

    public MySpacesMenu(int syncId, ServerPlayerEntity player) {
        super(syncId, player);

        infos = SpaceManager.getOwnedSpaces(player);

        if (infos.size() > 26) {
            infos = infos.subList(0, 26);
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

        if (infos.size() < 5) {
            setStack(26, createSpaceItem());
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

        if (slotIndex == 26 && infos.size() < 5) {
            SpaceInfo info = new SpaceInfo(SpaceManager.lastId++);
            info.name = player.getGameProfile().getName() + "'s New Space";
            info.icon = Items.PAPER;
            info.owner = player.getUuid();
            info.developers = new HashSet<>();
            info.builders = new HashSet<>();
            SpaceManager.info.put(info.id, info);
            MySpacesMenu.open(this.player);
            return;
        }
    }

    public static void open(ServerPlayerEntity player) {
        player.openHandledScreen(new NamedScreenHandlerFactory() {
            @Override
            public Text getDisplayName() {
                return Text.literal("My Spaces");
            }

            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new MySpacesMenu(syncId, (ServerPlayerEntity) player);
            }
        });
    }

    private static ItemStack createSpaceItem() {
        ItemStack item = new ItemStack(Items.GREEN_STAINED_GLASS);
        item.set(DataComponentTypes.ITEM_NAME, Text.literal("Create Space").withColor(ColourPalette.MINT_LIGHT_2.rgb24));
        item.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("Click to create").setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.GRAY)),
                Text.literal("a new space.").setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.GRAY))
        )));
        return item;
    }
}
