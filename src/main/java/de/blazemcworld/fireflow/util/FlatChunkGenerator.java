package de.blazemcworld.fireflow.util;

import de.blazemcworld.fireflow.FireFlow;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class FlatChunkGenerator extends FlatLevelSource {
    public FlatChunkGenerator() {
        super(new FlatLevelGeneratorSettings(
                Optional.of(HolderSet.empty()),
                FireFlow.server.reloadableRegistries().lookup()
                        .lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS),
                List.of()
        ));
        for (int i = 0; i < 59; i++) {
            settings().getLayers().add(Blocks.AIR.defaultBlockState());
        }
        settings().getLayers().addAll(List.of(
                Blocks.BEDROCK.defaultBlockState(),
                Blocks.DIRT.defaultBlockState(),
                Blocks.DIRT.defaultBlockState(),
                Blocks.DIRT.defaultBlockState(),
                Blocks.GRASS_BLOCK.defaultBlockState()
        ));
    }

    @Override
    public @NonNull CompletableFuture<ChunkAccess> fillFromNoise(@NonNull Blender blender, @NonNull RandomState noiseConfig, @NonNull StructureManager structureAccessor, ChunkAccess chunk) {
        if (chunk.getPos().x() < -32 || chunk.getPos().x() >= 32 || chunk.getPos().z() < -32 || chunk.getPos().z() >= 32) {
            return CompletableFuture.completedFuture(chunk);
        }
        return super.fillFromNoise(blender, noiseConfig, structureAccessor, chunk);
    }
}
