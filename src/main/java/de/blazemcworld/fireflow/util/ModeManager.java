package de.blazemcworld.fireflow.util;

import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.space.Lobby;
import de.blazemcworld.fireflow.space.PlayWorld;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

import java.util.WeakHashMap;

public class ModeManager {

    private static final WeakHashMap<ServerPlayerEntity, Mode> modes = new WeakHashMap<>();
    public static WeakHashMap<ServerPlayerEntity, TeleportTarget> respawnOverwrite = new WeakHashMap<>();

    static {
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            modes.put(newPlayer, modes.get(oldPlayer));
        });
    }

    public static Mode getFor(ServerPlayerEntity player) {
        return modes.getOrDefault(player, Mode.LOBBY);
    }

    public static void move(ServerPlayerEntity player, Mode mode, Space space) {
        runOnWorld(player.getServerWorld(), () -> handleExit(player));

        space = space != null ? space : SpaceManager.getSpaceForPlayer(player);
        if (space == null) mode = Mode.LOBBY;

        if (mode == Mode.LOBBY) {
            runOnWorld(Lobby.world, () -> {
                ServerPlayerEntity newPlayer = transfer(player, Lobby.world);
                modes.put(newPlayer, Mode.LOBBY);
                Lobby.onSpawn(newPlayer);
            });
            return;
        }

        if (mode == Mode.CODE) {
            Space lambdaSpace = space;
            runOnWorld(space.codeWorld, () -> {
                ServerPlayerEntity newPlayer = transfer(player, lambdaSpace.codeWorld);
                modes.put(newPlayer, Mode.CODE);
                lambdaSpace.editor.enterCode(newPlayer);
            });
            return;
        }

        if (mode == Mode.BUILD) {
            Space lambdaSpace = space;
            runOnWorld(space.playWorld, () -> {
                ServerPlayerEntity newPlayer = transfer(player, lambdaSpace.playWorld);
                modes.put(newPlayer, Mode.BUILD);
                lambdaSpace.enterBuild(newPlayer);
            });
            return;
        }

        Space lambdaSpace = space;
        runOnWorld(space.playWorld, () -> {
            ServerPlayerEntity newPlayer = transfer(player, lambdaSpace.playWorld);
            modes.put(newPlayer, Mode.PLAY);
            lambdaSpace.enterPlay(newPlayer);
        });
    }

    private static void runOnWorld(ServerWorld world, Runnable task) {
        if (world.thread != Thread.currentThread()) {
            if (world instanceof PlayWorld s) {
                s.submit(task);
                return;
            }
            FireFlow.server.execute(task);
            return;
        }
        task.run();
    }

    public static void handleExit(ServerPlayerEntity player) {
        Space space = SpaceManager.getSpaceForPlayer(player);
        Mode mode = getFor(player);

        if (mode == Mode.CODE && space != null) {
            space.editor.exitCode(player);
        }
        if (mode == Mode.PLAY && space != null) {
            space.playWorld.submit(() -> {
                space.evaluator.exitPlay(player);
            });
        }
    }

    private static ServerPlayerEntity transfer(ServerPlayerEntity oldPlayer, ServerWorld world) {
        respawnOverwrite.put(oldPlayer, new TeleportTarget(world, new Vec3d(0, 1, 0), Vec3d.ZERO, 0, 0, TeleportTarget.NO_OP));
        ServerPlayerEntity newPlayer = FireFlow.server.getPlayerManager().respawnPlayer(oldPlayer, true, Entity.RemovalReason.CHANGED_DIMENSION);
        newPlayer.networkHandler.player = newPlayer;
        Statistics.reset(newPlayer);
        return newPlayer;
    }

    public static void onJoinedServer(ServerPlayerEntity player) {
        modes.put(player, Mode.LOBBY);
    }

    public enum Mode {
        PLAY,
        BUILD,
        CODE,
        LOBBY
    }

}
