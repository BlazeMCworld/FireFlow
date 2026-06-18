package de.blazemcworld.fireflow.space;

import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.inventory.ActiveSpacesMenu;
import de.blazemcworld.fireflow.inventory.MySpacesMenu;
import de.blazemcworld.fireflow.messages.ColourPalette;
import de.blazemcworld.fireflow.util.WorldUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Set;

public class Lobby {

    public static ServerWorld world;
    public static Vec3d spawnPos;

    public static void init() {
        world = FireFlow.server.getOverworld();

        WorldUtil.setGameRules(world);
        world.setSpawnPos(BlockPos.ORIGIN, 0f);

        try {
            if (FireFlow.server instanceof MinecraftDedicatedServer dedicated) {
                String spawn = dedicated.getProperties().properties.getProperty("fireflow-spawn");
                if (spawn != null) {
                    String[] split = spawn.split(",");
                    spawnPos = new Vec3d(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static ItemStack mySpacesItem() {
        ItemStack item = new ItemStack(Items.ENCHANTED_BOOK);
        item.set(DataComponentTypes.ITEM_NAME, Text.literal("My Spaces").withColor(ColourPalette.ROSE_LIGHT_2.rgb24));
        item.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("Manage your spaces").setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.GRAY)),
                Text.literal("using this item.").setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.GRAY))
        )));
        return item;
    }

    private static ItemStack activeSpacesItem() {
        ItemStack item = new ItemStack(Items.BLAZE_POWDER);
        item.set(DataComponentTypes.ITEM_NAME, Text.literal("Active Spaces").withColor(ColourPalette.LIME_LIGHT_2.rgb24));
        item.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("View currently played on").setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.GRAY)),
                Text.literal("spaces using this item.").setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.GRAY))
        )));
        return item;
    }

    public static void onSpawn(ServerPlayerEntity player) {
        player.getInventory().setStack(0, mySpacesItem());
        player.getInventory().setStack(4, activeSpacesItem());
        player.setInvulnerable(true);

        if (spawnPos != null) {
            player.teleport(world, spawnPos.x, spawnPos.y, spawnPos.z, Set.of(), 0, 0, true);
        }
    }

    public static void onUseItem(ServerPlayerEntity player, ItemStack stack) {
        if (ItemStack.areItemsAndComponentsEqual(stack, mySpacesItem())) {
            MySpacesMenu.open(player);
            return;
        }
        if (ItemStack.areItemsAndComponentsEqual(stack, activeSpacesItem())) {
            ActiveSpacesMenu.open(player);
            return;
        }
    }
}
