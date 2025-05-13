package de.blazemcworld.fireflow.mixin;

import de.blazemcworld.fireflow.space.PlayWorld;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.OffThreadException;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.thread.ThreadExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkThreadUtils.class)
public abstract class NetworkThreadUtilsMixin {

    @Inject(method = "forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", at = @At("HEAD"), cancellable = true)
    private static <T extends PacketListener> void queuePacket(Packet<T> packet, T listener, ThreadExecutor<?> engine, CallbackInfo ci) {
        if (listener instanceof ServerPlayNetworkHandler playNetwork
                && playNetwork.player.getServerWorld() instanceof PlayWorld playWorld) {
            if (playWorld.thread == Thread.currentThread()) {
                ci.cancel();
                return;
            }

            playWorld.submit(() -> {
                try {
                    if (listener.accepts(packet)) packet.apply(listener);
                } catch (OffThreadException ignore) {}
            });

            throw OffThreadException.INSTANCE;
        }
    }

}
