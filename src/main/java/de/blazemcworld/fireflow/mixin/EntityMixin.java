package de.blazemcworld.fireflow.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TeleportTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "teleportCrossDimension", at = @At("HEAD"), cancellable = true)
    private void handleTeleport(ServerWorld world, TeleportTarget teleportTarget, CallbackInfoReturnable<Entity> cir) {
        cir.setReturnValue((Entity) (Object) this);
    }
}
