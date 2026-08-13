package de.blazemcworld.fireflow.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.level.GameType;

import java.util.Optional;

public class Statistics {

    public static void reset(ServerPlayer player) {
        AttributeMap attrs = player.getAttributes();
        for (AttributeInstance attr : attrs.getAttributesToSync()) {
            attr.removeModifiers();
        }
        player.removeAllEffects();
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(5);
        player.setHealth(player.getMaxHealth());
        GameType.ADVENTURE.updatePlayerAbilities(player.getAbilities());
        player.onUpdateAbilities();
        player.setGameMode(GameType.ADVENTURE);
        player.onUpdateAbilities();
        player.getInventory().clearContent();
        player.inventoryMenu.getCraftSlots().clearContent();
        player.totalExperience = 0;
        player.experienceProgress = 0;
        player.setExperienceLevels(0);
        player.setScore(0);
        player.getEnderChestInventory().clearContent();
        player.setLastDeathLocation(Optional.empty());
        player.setInvulnerable(false);
        player.setInvisible(false);
    }

}
