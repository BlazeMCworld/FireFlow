package de.blazemcworld.fireflow.mixin;

import net.minecraft.network.OffThreadException;
import net.minecraft.network.listener.ServerCrashSafePacketListener;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCrashSafePacketListener.class)
public interface ServerCrashSavePacketListenerMixin {

    @Inject(method = "onPacketException", at = @At("HEAD"), cancellable = true)
    default void onPacketException(Packet<?> packet, Exception exception, CallbackInfo ci) {
        if (exception == OffThreadException.INSTANCE) ci.cancel();
    }

}
