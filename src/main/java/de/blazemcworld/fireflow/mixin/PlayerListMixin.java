package de.blazemcworld.fireflow.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Inject(method = "save", at = @At("HEAD"), cancellable = true)
    private void fireflow$dontSave(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "loadPlayerData", at = @At("HEAD"), cancellable = true)
    private void fireflow$dontLoad(NameAndId nameAndId, CallbackInfoReturnable<Optional<CompoundTag>> cir) {
        cir.setReturnValue(Optional.empty());
    }

}
