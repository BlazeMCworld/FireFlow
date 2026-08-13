package de.blazemcworld.fireflow.mixin;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerPacketListener;
import net.minecraft.server.RunningOnDifferentThreadException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPacketListener.class)
public interface ServerCrashSavePacketListenerMixin {

    @Inject(method = "onPacketError", at = @At("HEAD"), cancellable = true)
    default void ofireflow$PacketError(Packet<?> packet, Exception e, CallbackInfo ci) {
        if (e == RunningOnDifferentThreadException.RUNNING_ON_DIFFERENT_THREAD) ci.cancel();
    }

}
