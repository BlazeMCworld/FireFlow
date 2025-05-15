package de.blazemcworld.fireflow.code.value;

import de.blazemcworld.fireflow.code.CodeThread;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class EntityValue {

    public final UUID uuid;
    private WeakReference<Entity> cache = null;

    public EntityValue(Entity entity) {
        if (entity == null || entity instanceof PlayerEntity) {
            uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
            return;
        }
        uuid = entity.getUuid();
        cache = new WeakReference<>(entity);
    }

    public EntityValue(UUID uuid) {
        this.uuid = uuid;
    }

    public Entity resolve(ServerWorld world) {
        Entity cache = this.cache == null ? null : this.cache.get();
        if (cache != null && cache.getWorld() == world && !cache.isRemoved()) return cache;
        Entity e = world.getEntity(uuid);
        if (e == null || e instanceof ServerPlayerEntity) return null;
        this.cache = new WeakReference<>(e);
        return e;
    }

    public <T> T apply(ServerWorld world, Function<Entity, T> fn, T fallback) {
        Entity e = resolve(world);
        return e == null ? fallback : fn.apply(e);
    }

    public <T> T apply(CodeThread ctx, Function<Entity, T> fn, T fallback) {
        return apply(ctx.evaluator.world, fn, fallback);
    }

    public void use(ServerWorld world, Consumer<Entity> cb) {
        Entity e = resolve(world);
        if (e != null) cb.accept(e);
    }

    public void use(CodeThread ctx, Consumer<Entity> cb) {
        use(ctx.evaluator.world, cb);
    }
}
