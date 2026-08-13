package de.blazemcworld.fireflow.space;

import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.inventory.ActiveSpacesMenu;
import de.blazemcworld.fireflow.inventory.MySpacesMenu;
import de.blazemcworld.fireflow.util.LevelUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;

public class Lobby {

    public static ServerLevel level;
    public static Vec3 spawnPos;

    public static void init() {
        level = FireFlow.server.overworld();

        LevelUtil.setGameRules(level);
        level.setRespawnData(LevelData.RespawnData.DEFAULT);

        try {
            if (FireFlow.server instanceof DedicatedServer dedicated) {
                String spawn = dedicated.getProperties().properties.getProperty("fireflow-spawn");
                if (spawn != null) {
                    String[] split = spawn.split(",");
                    spawnPos = new Vec3(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static ItemStack mySpacesItem() {
        ItemStack item = new ItemStack(Items.ENCHANTED_BOOK);
        item.set(DataComponents.ITEM_NAME, Component.literal("My Spaces").withColor(TextColor.LIGHT_PURPLE));
        item.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("Manage your spaces").setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.GRAY)),
                Component.literal("using this item.").setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.GRAY))
        )));
        return item;
    }

    private static ItemStack activeSpacesItem() {
        ItemStack item = new ItemStack(Items.BLAZE_POWDER);
        item.set(DataComponents.ITEM_NAME, Component.literal("Active Spaces").withColor(TextColor.GREEN));
        item.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("View currently played on").setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.GRAY)),
                Component.literal("spaces using this item.").setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.GRAY))
        )));
        return item;
    }

    public static void onSpawn(ServerPlayer player) {
        player.getInventory().setItem(0, mySpacesItem());
        player.getInventory().setItem(4, activeSpacesItem());
        player.setInvulnerable(true);

        if (spawnPos != null) {
            player.teleportTo(level, spawnPos.x, spawnPos.y, spawnPos.z, Set.of(), 0, 0, true);
        }
    }

    public static void onUseItem(ServerPlayer player, ItemStack stack) {
        if (ItemStack.isSameItemSameComponents(stack, mySpacesItem())) {
            MySpacesMenu.open(player);
            return;
        }
        if (ItemStack.isSameItemSameComponents(stack, activeSpacesItem())) {
            ActiveSpacesMenu.open(player);
            return;
        }
    }
}
