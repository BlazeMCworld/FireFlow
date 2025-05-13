package de.blazemcworld.fireflow.mixin;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

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

}
