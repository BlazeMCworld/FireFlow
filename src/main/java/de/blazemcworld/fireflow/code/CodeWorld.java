package de.blazemcworld.fireflow.code;

import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.util.WorldUtil;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.WorldGenerationProgressLogger;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.level.UnmodifiableLevelProperties;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.BooleanSupplier;

public class CodeWorld extends ServerWorld {

    public final Space space;
    private final Identifier id;
    public static final Set<ServerEntityManager<?>> savingDisabled = Collections.newSetFromMap(new WeakHashMap<>());

    public static CodeWorld create(String id, Space space) {
        return new CodeWorld(Identifier.of("fireflow", id), space);
    }

    private CodeWorld(Identifier id, Space space) {
        super(
                FireFlow.server, FireFlow.server.workerExecutor, FireFlow.server.session,
                new UnmodifiableLevelProperties(FireFlow.server.getSaveProperties(), FireFlow.server.getSaveProperties().getMainWorldProperties()), RegistryKey.of(RegistryKeys.WORLD, id),
                new DimensionOptions(
                        FireFlow.server.getCombinedDynamicRegistries().getCombinedRegistryManager()
                                .getOrThrow(RegistryKeys.DIMENSION_TYPE).getOrThrow(DimensionTypes.OVERWORLD),
                        new CodeChunkGenerator()
                ),
                WorldGenerationProgressLogger.noSpawnChunks(), false, 42, Collections.emptyList(), false, new RandomSequencesState(42)
        );
        this.id = id;
        this.space = space;

        FireFlow.server.worlds.put(RegistryKey.of(RegistryKeys.WORLD, id), this);
        WorldUtil.setGameRules(this);

        synchronized (savingDisabled) {
            savingDisabled.add(entityManager);
        }
    }

    @Override
    public void close() throws IOException {
        FireFlow.server.worlds.remove(RegistryKey.of(RegistryKeys.WORLD, id), this);
        super.close();
    }

    @Override
    public void save(@Nullable ProgressListener progressListener, boolean flush, boolean savingDisabled) {
    }

    @Override
    public void tick(BooleanSupplier shouldKeepTicking) {
        Set<Chunk> needsLoad = new HashSet<>();
        for (Entity entity : iterateEntities()) {
            needsLoad.add(getWorldChunk(entity.getBlockPos()));
        }
        for (long current : getForcedChunks()) {
            Chunk c = getChunk(ChunkPos.getPackedX(current), ChunkPos.getPackedZ(current));
            if (needsLoad.remove(c)) continue;
            setChunkForced(c.getPos().x, c.getPos().z, false);
        }
        for (Chunk c : needsLoad) {
            setChunkForced(c.getPos().x, c.getPos().z, true);
        }
        super.tick(shouldKeepTicking);
        space.editor.tick();
    }
}
