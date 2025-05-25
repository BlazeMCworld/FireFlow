package de.blazemcworld.fireflow.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceManager;
import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HungerManager.class)
public class HungerManagerMixin {

    @Redirect(method = "update", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/HungerManager;foodLevel:I", opcode = Opcodes.PUTFIELD))
    public void decrementFood(HungerManager instance, int newValue, @Local(argsOnly = true) ServerPlayerEntity player) {
        if (instance.getFoodLevel() <= newValue) return;

        Space space = SpaceManager.getSpaceForPlayer(player);
        ModeManager.Mode mode = ModeManager.getFor(player);
        if (space != null && mode == ModeManager.Mode.PLAY) {
            if (space.evaluator.onLoseFood(player, instance.getFoodLevel(), newValue)) return;
        }
        if (mode == ModeManager.Mode.LOBBY) return;

        instance.setFoodLevel(newValue);
    }

    @Redirect(method = "update", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/HungerManager;saturationLevel:F", opcode = Opcodes.PUTFIELD))
    public void decrementSaturation(HungerManager instance, float newValue, @Local(argsOnly = true) ServerPlayerEntity player) {
        if (instance.getSaturationLevel() <= newValue) return;

        Space space = SpaceManager.getSpaceForPlayer(player);
        ModeManager.Mode mode = ModeManager.getFor(player);
        if (space != null && mode == ModeManager.Mode.PLAY) {
            if (space.evaluator.onLoseSaturation(player, instance.getSaturationLevel(), newValue)) return;
        }
        if (mode == ModeManager.Mode.LOBBY) return;

        instance.setSaturationLevel(newValue);
    }

}
