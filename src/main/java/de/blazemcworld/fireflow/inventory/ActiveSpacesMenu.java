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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ActiveSpacesMenu extends InventoryMenu {

    private final List<SpaceInfo> infos = new ArrayList<>();

    public ActiveSpacesMenu(int syncId, ServerPlayer player) {
        super(syncId, player);

        for (Space s : SpaceManager.activeSpaces()) {
            infos.add(s.info);
            if (infos.size() >= 26) break;
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
    }

    @Override
    public void clicked(int slotIndex, int button, @NonNull ContainerInput actionType, @NonNull Player player) {
        if (this.player != player) return;

        if (slotIndex >= 0 && slotIndex < infos.size()) {
            SpaceInfo info = infos.get(slotIndex);
            ModeManager.move(this.player, ModeManager.Mode.PLAY, SpaceManager.getOrLoadSpace(info));
            return;
        }
    }

    public static void open(ServerPlayer player) {
        player.openMenu(new MenuProvider() {
            @Override
            public @NonNull Component getDisplayName() {
                return Component.literal("Active Spaces");
            }

            @Override
            public AbstractContainerMenu createMenu(int syncId, @NonNull Inventory inv, @NonNull Player player) {
                return new ActiveSpacesMenu(syncId, (ServerPlayer) player);
            }
        });
    }
}
