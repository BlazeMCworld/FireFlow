package de.blazemcworld.fireflow.mixin;

import de.blazemcworld.fireflow.code.CodeWorld;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.world.storage.ChunkDataAccess;
import net.minecraft.world.storage.ChunkDataList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerEntityManager.class)
public class ServerEntityManagerMixin {

    @Redirect(method = "trySave", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/ChunkDataAccess;writeChunkData(Lnet/minecraft/world/storage/ChunkDataList;)V"))
    private <T> void maybeSave(ChunkDataAccess<T> instance, ChunkDataList<T> data) {
        synchronized (CodeWorld.savingDisabled) {
            if (CodeWorld.savingDisabled.contains(this)) return;
        }
        instance.writeChunkData(data);
    }

}
