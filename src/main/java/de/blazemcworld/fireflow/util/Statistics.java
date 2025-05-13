package de.blazemcworld.fireflow.util;

import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

import java.util.Optional;

public class Statistics {

    public static void reset(ServerPlayerEntity player) {
        AttributeContainer attrs = player.getAttributes();
        for (EntityAttributeInstance attr : attrs.getTracked()) {
            attr.clearModifiers();
        }
        player.clearStatusEffects();
        player.getHungerManager().setFoodLevel(20);
        player.getHungerManager().setSaturationLevel(5);
        player.setHealth(player.getMaxHealth());
        GameMode.ADVENTURE.setAbilities(player.getAbilities());
        player.sendAbilitiesUpdate();
        player.changeGameMode(GameMode.ADVENTURE);
        player.sendAbilitiesUpdate();
        player.getInventory().clear();
        player.playerScreenHandler.getCraftingInput().clear();
        player.totalExperience = 0;
        player.experienceProgress = 0;
        player.setExperienceLevel(0);
        player.setScore(0);
        player.getEnderChestInventory().clear();
        player.setLastDeathPos(Optional.empty());
        player.setInvulnerable(false);
    }

}
