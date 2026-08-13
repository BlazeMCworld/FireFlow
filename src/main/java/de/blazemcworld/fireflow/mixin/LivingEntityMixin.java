package de.blazemcworld.fireflow.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @ModifyVariable(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSleeping()Z"), argsOnly = true, order = 900, name = "damage")
    private float fireflow$changeDamage(float damage, @Local(argsOnly = true, name = "source") DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;
        Space space = SpaceManager.getSpaceForLevel((ServerLevel) self.level());
        if (space != null && space.playLevel == self.level()) {
            return space.evaluator.adjustDamage(self, source, damage);
        }
        return damage;
    }

}
