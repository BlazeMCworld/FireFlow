package de.blazemcworld.fireflow.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "teleportCrossDimension", at = @At("HEAD"), cancellable = true)
    private void fireflow$handleTeleport(ServerLevel oldLevel, ServerLevel newLevel, TeleportTransition transition, CallbackInfoReturnable<Entity> cir) {
        cir.setReturnValue((Entity) (Object) this);
    }
}
