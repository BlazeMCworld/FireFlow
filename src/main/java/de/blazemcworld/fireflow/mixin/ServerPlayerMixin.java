package de.blazemcworld.fireflow.mixin;

import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Shadow
    public abstract ServerLevel level();

    @Inject(method = "findRespawnPositionAndUseSpawnBlock", at = @At("HEAD"), cancellable = true)
    private void fireflow$respawnInSameWorld(boolean consumeSpawnBlock, TeleportTransition.PostTeleportTransition postTeleportTransition, CallbackInfoReturnable<TeleportTransition> cir) {
        TeleportTransition overwrite = ModeManager.respawnOverwrite.remove(this);
        if (overwrite != null) {
            cir.setReturnValue(overwrite);
            return;
        }
        cir.setReturnValue(new TeleportTransition(level(), Vec3.ZERO, Vec3.ZERO, 0, 0, TeleportTransition.DO_NOTHING));
    }

    @Inject(method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;", at = @At("HEAD"), cancellable = true)
    private void fireflow$preventTeleport(TeleportTransition transition, CallbackInfoReturnable<ServerPlayer> cir) {
        if (level() == transition.newLevel()) return;
        cir.setReturnValue((ServerPlayer) (Object) this);
    }
}
