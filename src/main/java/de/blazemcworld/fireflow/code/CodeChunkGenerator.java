package de.blazemcworld.fireflow.code;

import de.blazemcworld.fireflow.FireFlow;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;

import java.util.List;
import java.util.Optional;

public class CodeChunkGenerator extends net.minecraft.world.gen.chunk.FlatChunkGenerator {
    public CodeChunkGenerator() {
        super(new FlatChunkGeneratorConfig(
                Optional.of(RegistryEntryList.empty()),
                FireFlow.server.getCombinedDynamicRegistries().getCombinedRegistryManager()
                        .getOrThrow(RegistryKeys.BIOME).getOrThrow(BiomeKeys.PLAINS),
                List.of()
        ));
    }

    @Override
    public void generateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor) {
        if (chunk.getPos().z != 1) return;

        for (int x = 0; x < 16; x++) {
            for (int y = chunk.getBottomY(); y <= chunk.getTopYInclusive(); y++) {
                chunk.setBlockState(new BlockPos(x, y, 0), Blocks.POLISHED_BLACKSTONE.getDefaultState());
            }
        }
    }
}
