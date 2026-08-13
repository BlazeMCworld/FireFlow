package de.blazemcworld.fireflow.mixin;

import de.blazemcworld.fireflow.space.PlayLevel;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketProcessor;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PacketUtils.class)
public abstract class PacketUtilsMixin {

    @Inject(method = "ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V", at = @At("HEAD"), cancellable = true)
    private static <T extends PacketListener> void fireflow$queuePacket(Packet<T> packet, T listener, PacketProcessor packetProcessor, CallbackInfo ci) {
        if (listener instanceof ServerGamePacketListenerImpl playNetwork
                && playNetwork.player.level() instanceof PlayLevel playLevel) {
            if (playLevel.thread == Thread.currentThread()) {
                ci.cancel();
                return;
            }

            playLevel.submit(() -> {
                try {
                    if (listener.shouldHandleMessage(packet)) packet.handle(listener);
                } catch (RunningOnDifferentThreadException ignore) {}
            });

            throw RunningOnDifferentThreadException.RUNNING_ON_DIFFERENT_THREAD;
        }
    }

}
