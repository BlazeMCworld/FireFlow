package de.blazemcworld.fireflow.code;

import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.util.LevelUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.storage.DerivedLevelData;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.BooleanSupplier;

public class CodeLevel extends ServerLevel {

    public final Space space;
    private final Identifier id;
    public static final Set<PersistentEntitySectionManager<?>> savingDisabled = Collections.newSetFromMap(new WeakHashMap<>());

    public static CodeLevel create(String id, Space space) {
        return new CodeLevel(Identifier.fromNamespaceAndPath("fireflow", id), space);
    }

    private CodeLevel(Identifier id, Space space) {
        super(
                FireFlow.server, FireFlow.server.executor, FireFlow.server.storageSource,
                new DerivedLevelData(FireFlow.server.getWorldData(), FireFlow.server.getWorldData().overworldData()),
                ResourceKey.create(Registries.DIMENSION, id),
                new LevelStem(
                        FireFlow.server.reloadableRegistries().lookup()
                                .lookupOrThrow(Registries.DIMENSION_TYPE).getOrThrow(BuiltinDimensionTypes.OVERWORLD),
                        new CodeChunkGenerator()
                ),
                false, 42, Collections.emptyList(), false
        );
        this.id = id;
        this.space = space;

        FireFlow.server.levels.put(ResourceKey.create(Registries.DIMENSION, id), this);
        LevelUtil.setGameRules(this);

        synchronized (savingDisabled) {
            savingDisabled.add(entityManager);
        }
    }

    @Override
    public void close() throws IOException {
        FireFlow.server.levels.remove(ResourceKey.create(Registries.DIMENSION, id), this);
        super.close();
    }

    @Override
    public boolean noSave() {
        return true;
    }

    @Override
    public void save(@Nullable ProgressListener progressListener, boolean flush, boolean savingDisabled) {
    }

    @Override
    public void tick(@NonNull BooleanSupplier shouldKeepTicking) {
        Set<ChunkAccess> needsLoad = new HashSet<>();
        for (Entity entity : getAllEntities()) {
            needsLoad.add(getChunk(entity.blockPosition()));
        }
        for (long current : getForceLoadedChunks()) {
            ChunkAccess c = getChunk(ChunkPos.getX(current), ChunkPos.getZ(current));
            if (needsLoad.remove(c)) continue;
            setChunkForced(c.getPos().x(), c.getPos().z(), false);
        }
        for (ChunkAccess c : needsLoad) {
            setChunkForced(c.getPos().x(), c.getPos().z(), true);
        }
        super.tick(shouldKeepTicking);
        space.editor.tick();
    }
}
