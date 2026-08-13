package de.blazemcworld.fireflow.space;

import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.DerivedLevelData;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;

public class PlayLevel extends ServerLevel {

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

    public static PlayLevel create(String id, Space space) {
        return new PlayLevel(Identifier.fromNamespaceAndPath("fireflow", id), new TodoListExecutor(), space);
    }

    private PlayLevel(Identifier id, TodoListExecutor exec, Space space) {
        super(
                FireFlow.server, exec, FireFlow.server.storageSource,
                new DerivedLevelData(FireFlow.server.getWorldData(), FireFlow.server.getWorldData().overworldData()),
                ResourceKey.create(Registries.DIMENSION, id),
                new LevelStem(
                        FireFlow.server.reloadableRegistries().lookup()
                                .lookupOrThrow(Registries.DIMENSION_TYPE).getOrThrow(BuiltinDimensionTypes.OVERWORLD),
                        new FlatChunkGenerator()
                ),
                false, 42, Collections.emptyList(), false
        );
        this.exec = exec;
        this.id = id;
        FireFlow.server.levels.put(ResourceKey.create(Registries.DIMENSION, id), this);

        LevelUtil.setGameRules(this);

        this.space = space;

        Thread tickWorker = new Thread(this::tickLoop);
        tickWorker.setName("FireFlow-Tick-" + id);
        tickWorker.setPriority(Thread.MIN_PRIORITY);
        tickWorker.setDaemon(true);
        tickWorker.start();
        Thread todoWorker = new Thread(this::todoLoop);
        todoWorker.setName("FireFlow-Todo-" + id);
        todoWorker.setPriority(Thread.MIN_PRIORITY);
        todoWorker.setDaemon(true);
        todoWorker.start();
    }

    public void markStarted() {
        started = true;
    }

    private void tickLoop() {
        thread = Thread.currentThread();
        getChunkSource().mainThread = thread;

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
    public void tick(@NonNull BooleanSupplier shouldKeepTicking) {
        ticksBehind++;
        cpuMeasures.removeIf(m -> !m.isRecent());

        if (active && lastTick + 10000 < System.currentTimeMillis() && thread.isAlive()) {
            StringBuilder stack = new StringBuilder();
            for (StackTraceElement e : thread.getStackTrace()) {
                stack.append(e).append("\n");
            }

            SpaceManager.unloadSpace(space, null);
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
        if (FireFlow.server.isShutdown()) return;
        lastTick = System.currentTimeMillis();
        super.tick(() -> ticksBehind == 0);
        for (ServerPlayer p : new ArrayList<>(players())) {
            p.connection.tick();
            p.connection.chunkSender.sendNextChunks(p);
            p.connection.resumeFlushing();
        }
        space.evaluator.tick();
        irregularTick();
    }

    @Override
    public void tickNonPassenger(@NonNull Entity entity) {
        if (!active && !started) return;
        super.tickNonPassenger(entity);
    }

    private void irregularTick() {
        while (!tickTodo.isEmpty() && active) {
            Runnable task;
            synchronized (tickTodo) {
                task = tickTodo.removeFirst();
            }
            task.run();
        }
        while (getChunkSource().pollTask() && active) {
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
        FireFlow.server.levels.remove(ResourceKey.create(Registries.DIMENSION, id), this);
        for (ServerPlayer p : new ArrayList<>(players())) {
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
