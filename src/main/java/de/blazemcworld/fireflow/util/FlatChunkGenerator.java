package de.blazemcworld.fireflow.util;

import de.blazemcworld.fireflow.FireFlow;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class FlatChunkGenerator extends net.minecraft.world.gen.chunk.FlatChunkGenerator {
    public FlatChunkGenerator() {
        super(new FlatChunkGeneratorConfig(
                Optional.of(RegistryEntryList.empty()),
                FireFlow.server.getCombinedDynamicRegistries().getCombinedRegistryManager()
                        .getOrThrow(RegistryKeys.BIOME).getOrThrow(BiomeKeys.PLAINS),
                List.of()
        ));
        for (int i = 0; i < 59; i++) {
            getConfig().getLayerBlocks().add(Blocks.AIR.getDefaultState());
        }
        getConfig().getLayerBlocks().addAll(List.of(
                Blocks.BEDROCK.getDefaultState(),
                Blocks.DIRT.getDefaultState(),
                Blocks.DIRT.getDefaultState(),
                Blocks.DIRT.getDefaultState(),
                Blocks.GRASS_BLOCK.getDefaultState()
        ));
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
        if (chunk.getPos().x < -32 || chunk.getPos().x >= 32 || chunk.getPos().z < -32 || chunk.getPos().z >= 32) {
            return CompletableFuture.completedFuture(chunk);
        }
        return super.populateNoise(blender, noiseConfig, structureAccessor, chunk);
    }
}
