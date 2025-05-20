package de.blazemcworld.fireflow.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @ModifyVariable(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSleeping()Z"), argsOnly = true, order = 900)
    private float changeDamage(float amount, @Local(argsOnly = true) DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;
        Space space = SpaceManager.getSpaceForWorld((ServerWorld) self.getWorld());
        if (space != null && space.playWorld == self.getWorld()) {
            return space.evaluator.adjustDamage(self, source, amount);
        };
        return amount;
    }

}
