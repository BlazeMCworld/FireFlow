package de.blazemcworld.fireflow.code.value;

import de.blazemcworld.fireflow.code.CodeThread;
import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class PlayerValue {

    public final UUID uuid;

    public PlayerValue(ServerPlayer player) {
        uuid = player.getUUID();
    }

    public PlayerValue(UUID uuid) {
        this.uuid = uuid;
    }

    public void use(ServerLevel level, Consumer<ServerPlayer> cb) {
        if (level.getPlayerByUUID(uuid) instanceof ServerPlayer p) {
            if (p.level() == level && ModeManager.getFor(p) == ModeManager.Mode.PLAY) {
                cb.accept(p);
                return;
            }
        }
        cb.accept(null);
    }

    public <T> T apply(ServerLevel level, Function<ServerPlayer, T> fn) {
        if (level.getPlayerByUUID(uuid) instanceof ServerPlayer p) {
            if (p.level() == level && ModeManager.getFor(p) == ModeManager.Mode.PLAY) {
                return fn.apply(p);
            }
        }
        return fn.apply(null);
    }

    public void tryUse(ServerLevel world, Consumer<ServerPlayer> cb) {
        use(world, (p) -> {
            if (p == null) return;
            cb.accept(p);
        });
    }

    public <T> T tryGet(ServerLevel world, Function<ServerPlayer, T> fn, T fallback) {
        return apply(world, p -> {
            if (p == null) return fallback;
            return fn.apply(p);
        });
    }

    public <T> T tryGet(CodeThread ctx, Function<ServerPlayer, T> fn, T fallback) {
        return tryGet(ctx.evaluator.level, fn, fallback);
    }

    public void tryUse(CodeThread ctx, Consumer<ServerPlayer> cb) {
        tryUse(ctx.evaluator.level, cb);
    }
}
