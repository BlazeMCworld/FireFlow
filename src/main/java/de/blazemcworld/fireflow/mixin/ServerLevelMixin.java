package de.blazemcworld.fireflow.mixin;

import com.mojang.datafixers.DataFixer;
import de.blazemcworld.fireflow.code.CodeLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/chunk/storage/RegionStorageInfo;Ljava/nio/file/Path;Lcom/mojang/datafixers/DataFixer;ZLnet/minecraft/util/datafix/DataFixTypes;)Lnet/minecraft/world/level/chunk/storage/SimpleRegionStorage;"))
    public SimpleRegionStorage fireflow$maybeBlockSaveLoad(RegionStorageInfo info, Path folder, DataFixer fixerUpper, boolean syncWrites, DataFixTypes dataFixType) {
        //noinspection ConstantValue
        if (!((Object) this instanceof CodeLevel)) {
            return new SimpleRegionStorage(info, folder, fixerUpper, syncWrites, dataFixType);
        }

        return new SimpleRegionStorage(info, folder, fixerUpper, syncWrites, dataFixType) {
            @Override
            public @NonNull CompletableFuture<Void> write(@NonNull ChunkPos pos, @NonNull CompoundTag value) {
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public @NonNull CompletableFuture<Void> write(@NonNull ChunkPos pos, @NonNull Supplier<CompoundTag> supplier) {
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public @NonNull CompletableFuture<Optional<CompoundTag>> read(@NonNull ChunkPos pos) {
                return CompletableFuture.completedFuture(Optional.empty());
            }
        };
    }

}
