package de.blazemcworld.fireflow.inventory;

import de.blazemcworld.fireflow.code.type.TextType;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceInfo;
import de.blazemcworld.fireflow.space.SpaceManager;
import de.blazemcworld.fireflow.util.ModeManager;
import de.blazemcworld.fireflow.util.ProfileApi;
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

import java.util.HashSet;
import java.util.List;

public class MySpacesMenu extends InventoryMenu {

    private List<SpaceInfo> infos;

    public MySpacesMenu(int syncId, ServerPlayer player) {
        super(syncId, player);

        infos = SpaceManager.getOwnedSpaces(player);

        if (infos.size() > 26) {
            infos = infos.subList(0, 26);
        }

        for (int i = 0; i < infos.size(); i++) {
            SpaceInfo info = infos.get(i);
            int players = 0;
            Space s = SpaceManager.getIfLoaded(info);
            if (s != null) players = s.playersPlayMode().size();

            ItemStack item = new ItemStack(info.icon);
            item.set(DataComponents.ITEM_NAME, TextType.INSTANCE.parseInset(info.name));
            Style loreStyle = Style.EMPTY.withItalic(false).withColor(ChatFormatting.GRAY);
            item.set(DataComponents.LORE, new ItemLore(List.of(
                    Component.literal("by " + ProfileApi.displayName(info.owner)).setStyle(loreStyle),
                    Component.literal("Players: " + players).setStyle(loreStyle),
                    Component.literal("ID: " + info.id).setStyle(loreStyle)
            )));

            setItem(i, item);
        }

        if (infos.size() < 5) {
            setItem(26, createSpaceItem());
        }
    }

    @Override
    public void clicked(int slotIndex, int button, @NonNull ContainerInput actionType, @NonNull Player player) {
        if (this.player != player) return;

        if (slotIndex >= 0 && slotIndex < infos.size()) {
            SpaceInfo info = infos.get(slotIndex);
            ModeManager.move(this.player, ModeManager.Mode.PLAY, SpaceManager.getOrLoadSpace(info));
            return;
        }

        if (slotIndex == 26 && infos.size() < 5) {
            SpaceInfo info = new SpaceInfo(SpaceManager.lastId++);
            info.name = player.getGameProfile().name() + "'s New Space";
            info.icon = Items.PAPER;
            info.owner = player.getUUID();
            info.developers = new HashSet<>();
            info.builders = new HashSet<>();
            SpaceManager.info.put(info.id, info);
            MySpacesMenu.open(this.player);
            return;
        }
    }

    public static void open(ServerPlayer player) {
        player.openMenu(new MenuProvider() {
            @Override
            public @NonNull Component getDisplayName() {
                return Component.literal("My Spaces");
            }

            @Override
            public AbstractContainerMenu createMenu(int syncId, @NonNull Inventory inv, @NonNull Player player) {
                return new MySpacesMenu(syncId, (ServerPlayer) player);
            }
        });
    }

    private static ItemStack createSpaceItem() {
        ItemStack item = new ItemStack(Items.STAINED_GLASS.green());
        item.set(DataComponents.ITEM_NAME, Component.literal("Create Space").withColor(TextColor.GREEN));
        item.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("Click to create").setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.GRAY)),
                Component.literal("a new space.").setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.GRAY))
        )));
        return item;
    }
}
