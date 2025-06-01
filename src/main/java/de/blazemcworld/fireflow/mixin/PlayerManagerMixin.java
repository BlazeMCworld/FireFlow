package de.blazemcworld.fireflow.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceManager;
import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Inject(method = "savePlayerData", at = @At("HEAD"), cancellable = true)
    private void dontSave(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "loadPlayerData", at = @At("HEAD"), cancellable = true)
    private void dontLoad(ServerPlayerEntity player, CallbackInfoReturnable<Optional<NbtCompound>> cir) {
        cir.setReturnValue(Optional.empty());
    }

    @Inject(method = "respawnPlayer", at = @At("HEAD"))
    private void onRespawn(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir) {
        Space space = SpaceManager.getSpaceForPlayer(player);
        if (space != null && ModeManager.getFor(player) == ModeManager.Mode.PLAY) space.evaluator.onRespawn(player);;
    }

}
