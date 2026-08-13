package de.blazemcworld.fireflow.code;

import de.blazemcworld.fireflow.FireFlow;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

public class CodeChunkGenerator extends FlatLevelSource {
    public CodeChunkGenerator() {
        super(new FlatLevelGeneratorSettings(
                Optional.of(HolderSet.empty()),
                FireFlow.server.reloadableRegistries().lookup()
                        .lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS),
                List.of()
        ));
    }

    @Override
    public void applyBiomeDecoration(@NonNull WorldGenLevel level, ChunkAccess chunk, @NonNull StructureManager structureManager) {
        if (chunk.getPos().z() != 1) return;

        for (int x = 0; x < 16; x++) {
            for (int y = chunk.getMinY(); y <= chunk.getMaxY(); y++) {
                chunk.setBlockState(new BlockPos(x, y, 0), Blocks.POLISHED_BLACKSTONE.defaultBlockState());
            }
        }
    }
}
