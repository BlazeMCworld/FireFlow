package de.blazemcworld.fireflow.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceManager;
import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FoodData.class)
public class FoodDataMixin {

    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/food/FoodData;foodLevel:I", opcode = Opcodes.PUTFIELD))
    public void fireflow$decrementFood(FoodData instance, int newValue, @Local(argsOnly = true, name = "player") ServerPlayer player) {
        if (instance.getFoodLevel() <= newValue) return;

        Space space = SpaceManager.getSpaceForPlayer(player);
        ModeManager.Mode mode = ModeManager.getFor(player);
        if (space != null && mode == ModeManager.Mode.PLAY) {
            if (space.evaluator.onLoseFood(player, instance.getFoodLevel(), newValue)) return;
        }
        if (mode == ModeManager.Mode.LOBBY) return;

        instance.setFoodLevel(newValue);
    }

    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/food/FoodData;saturationLevel:F", opcode = Opcodes.PUTFIELD))
    public void fireflow$decrementSaturation(FoodData instance, float newValue, @Local(argsOnly = true, name = "player") ServerPlayer player) {
        if (instance.getSaturationLevel() <= newValue) return;

        Space space = SpaceManager.getSpaceForPlayer(player);
        ModeManager.Mode mode = ModeManager.getFor(player);
        if (space != null && mode == ModeManager.Mode.PLAY) {
            if (space.evaluator.onLoseSaturation(player, instance.getSaturationLevel(), newValue)) return;
        }
        if (mode == ModeManager.Mode.LOBBY) return;

        instance.setSaturation(newValue);
    }

}
