package de.blazemcworld.fireflow.mixin;

import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Shadow public abstract ServerWorld getServerWorld();

    @Inject(method = "getRespawnTarget", at = @At("HEAD"), cancellable = true)
    private void respawnInSameWorld(boolean alive, TeleportTarget.PostDimensionTransition postDimensionTransition, CallbackInfoReturnable<TeleportTarget> cir) {
        TeleportTarget overwrite = ModeManager.respawnOverwrite.remove(this);
        if (overwrite != null) {
            cir.setReturnValue(overwrite);
            return;
        }
        cir.setReturnValue(new TeleportTarget(
                getServerWorld(), (ServerPlayerEntity) (Object) this, postDimensionTransition
        ).withPosition(Vec3d.ZERO));
    }

    @Inject(method = "teleportTo(Lnet/minecraft/world/TeleportTarget;)Lnet/minecraft/server/network/ServerPlayerEntity;", at = @At("HEAD"), cancellable = true)
    private void preventTeleport(TeleportTarget teleportTarget, CallbackInfoReturnable<ServerPlayerEntity> cir) {
        if (getServerWorld() == teleportTarget.world()) return;
        cir.setReturnValue((ServerPlayerEntity) (Object) this);
    }
}
