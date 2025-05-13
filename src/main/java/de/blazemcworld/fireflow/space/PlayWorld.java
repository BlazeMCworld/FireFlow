package de.blazemcworld.fireflow.space;

import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.util.*;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.WorldGenerationProgressLogger;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.level.UnmodifiableLevelProperties;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;

public class PlayWorld extends ServerWorld {

    public final Space space;
    private int ticksBehind = 0;
    private final TodoListExecutor exec;
    private final List<Runnable> tickTodo = new LinkedList<>();
    private final Identifier id;
    private boolean active = true;
    private boolean closed = false;
    private boolean started = false;
    private final Set<CpuMeasure> cpuMeasures = ConcurrentHashMap.newKeySet();
    private Runnable closeCallback = null;
    public long lastTick = System.currentTimeMillis();

    public static PlayWorld create(String id, Space space) {
        return new PlayWorld(Identifier.of("fireflow", id), new TodoListExecutor(), space);
    }

    private PlayWorld(Identifier id, TodoListExecutor exec, Space space) {
        super(
                FireFlow.server, exec, FireFlow.server.session,
                new UnmodifiableLevelProperties(FireFlow.server.getSaveProperties(), FireFlow.server.getSaveProperties().getMainWorldProperties()), RegistryKey.of(RegistryKeys.WORLD, id),
                new DimensionOptions(
                        FireFlow.server.getCombinedDynamicRegistries().getCombinedRegistryManager()
                                .getOrThrow(RegistryKeys.DIMENSION_TYPE).getOrThrow(DimensionTypes.OVERWORLD),
                        new FlatChunkGenerator()
                ),
                WorldGenerationProgressLogger.noSpawnChunks(), false, 42, Collections.emptyList(), false, new RandomSequencesState(42)
        );
        this.exec = exec;
        this.id = id;
        FireFlow.server.worlds.put(RegistryKey.of(RegistryKeys.WORLD, id), this);

        WorldUtil.setGameRules(this);

        this.space = space;

        Thread tickWorker = new Thread(this::tickLoop);
        tickWorker.setName("FireFlow-Tick" + id);
        tickWorker.setDaemon(true);
        tickWorker.start();
        Thread todoWorker = new Thread(this::todoLoop);
        todoWorker.setName("FireFlow-Todo" + id);
        todoWorker.setDaemon(true);
        todoWorker.start();
    }

    public void markStarted() {
        started = true;
    }

    private void tickLoop() {
        thread = Thread.currentThread();
        getChunkManager().serverThread = thread;

        while (active) {
            while (active) {
                synchronized (this) {
                    if (ticksBehind != 0) break;
                }
                shortPause();
                CpuMeasure m = new CpuMeasure();
                cpuMeasures.add(m);
                try {
                    irregularTick();
                } catch (Exception err) {
                    FireFlow.LOGGER.error("Error ticking {}", id, err);
                }
                m.finish();
            }
            if (!active) break;
            synchronized (this) {
                if (ticksBehind > 20) ticksBehind = 20;
                ticksBehind--;
            }
            CpuMeasure m = new CpuMeasure();
            cpuMeasures.add(m);
            try {
                fixedTick();
            } catch (Exception err) {
                FireFlow.LOGGER.error("Error ticking {}", id, err);
            }
            m.finish();
        }
        try {
            close();
        } catch (IOException err) {
            FireFlow.LOGGER.error("Error unloading {}", id, err);
        }
    }

    private void todoLoop() {
        while (!closed) {
            CpuMeasure m = new CpuMeasure();
            cpuMeasures.add(m);
            exec.work();
            m.finish();
            shortPause();
        }
    }

    private void shortPause() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException err) {
            FireFlow.LOGGER.error("Unexpected interrupt!", err);
        }
    }

    @Override
    public void tick(BooleanSupplier shouldKeepTicking) {
        ticksBehind++;
        cpuMeasures.removeIf(m -> !m.isRecent());

        if (active && lastTick + 10000 < System.currentTimeMillis() && thread.isAlive()) {
            StringBuilder stack = new StringBuilder();
            for (StackTraceElement e : thread.getStackTrace()) {
                stack.append(e).append("\n");
            }

            SpaceManager.unloadSpace(space);
            FireFlow.LOGGER.error("Space {} took too long to tick! Thread dump:\n{}", space.info.id, stack.toString());
        }
    }

    public long cpuMs() {
        long ns = 0;
        for (CpuMeasure m : cpuMeasures) {
            if (m.isRecent()) ns += m.getTimeNs();
        }
        return ns / 1_000_000;
    }

    private void fixedTick() {
        if (FireFlow.server.isStopping()) return;
        lastTick = System.currentTimeMillis();
        super.tick(() -> ticksBehind == 0);
        for (ServerPlayerEntity p : new ArrayList<>(getPlayers())) {
            p.networkHandler.tick();
            p.networkHandler.chunkDataSender.sendChunkBatches(p);
            p.networkHandler.enableFlush();
        }
        irregularTick();
    }

    @Override
    public void tickEntity(Entity entity) {
        if (!active && !(entity instanceof ServerPlayerEntity) && !started) return;
        super.tickEntity(entity);
    }

    private void irregularTick() {
        while (!tickTodo.isEmpty() && active) {
            Runnable task;
            synchronized (tickTodo) {
                task = tickTodo.removeFirst();
            }
            task.run();
        }
        while (getChunkManager().executeQueuedTasks() && active) {
        }
    }

    public void submit(Runnable r) {
        synchronized (tickTodo) {
            tickTodo.add(r);
        }
    }

    @Override
    public void close() throws IOException {
        if (closed) return;
        FireFlow.server.worlds.remove(RegistryKey.of(RegistryKeys.WORLD, id), this);
        for (ServerPlayerEntity p : new ArrayList<>(getPlayers())) {
            ModeManager.move(p, ModeManager.Mode.LOBBY, null);
        }
        super.close();
        closed = true;
        if (closeCallback != null) closeCallback.run();
    }

    public void closeSoon(Runnable r) {
        closeCallback = r;
        active = false;
    }
}
