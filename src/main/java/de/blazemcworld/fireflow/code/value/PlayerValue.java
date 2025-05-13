package de.blazemcworld.fireflow.code.value;

import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.code.CodeThread;
import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class PlayerValue {
    
    public final UUID uuid;

    public PlayerValue(ServerPlayerEntity player) {
        uuid = player.getUuid();
    }

    public PlayerValue(UUID uuid) {
        this.uuid = uuid;
    }

    public void use(ServerWorld world, Consumer<ServerPlayerEntity> cb) {
        ServerPlayerEntity p = FireFlow.server.getPlayerManager().getPlayer(uuid);
        if (p != null) {
            synchronized (p) {
                if (p.getServerWorld() == world && ModeManager.getFor(p) == ModeManager.Mode.PLAY) {
                    cb.accept(p);
                    return;
                }
            }
        }
        cb.accept(null);
    }

    public <T> T apply(ServerWorld world, Function<ServerPlayerEntity, T> fn) {
        ServerPlayerEntity p = FireFlow.server.getPlayerManager().getPlayer(uuid);
        if (p != null) {
            synchronized (p) {
                if (p.getServerWorld() == world && ModeManager.getFor(p) == ModeManager.Mode.PLAY) {
                    return fn.apply(p);
                }
            }
        }
        return fn.apply(null);
    }

    public void tryUse(ServerWorld world, Consumer<ServerPlayerEntity> cb) {
        use(world, (p) -> {
            if (p == null) return;
            cb.accept(p);
        });
    }

    public <T> T tryGet(ServerWorld world, Function<ServerPlayerEntity, T> fn, T fallback) {
        return apply(world, p -> {
            if (p == null) return fallback;
            return fn.apply(p);
        });
    }

    public <T> T tryGet(CodeThread ctx, Function<ServerPlayerEntity, T> fn, T fallback) {
        return tryGet(ctx.evaluator.world, fn, fallback);
    }

    public void tryUse(CodeThread ctx, Consumer<ServerPlayerEntity> cb) {
        tryUse(ctx.evaluator.world, cb);
    }
}
