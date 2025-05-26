package de.blazemcworld.fireflow.util;

import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.code.EditOrigin;
import de.blazemcworld.fireflow.code.node.impl.player.visual.SetPlayerSkinNode;
import de.blazemcworld.fireflow.space.Lobby;
import de.blazemcworld.fireflow.space.PlayWorld;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.WeakHashMap;

public class ModeManager {

    private static final HashMap<UUID, Mode> modes = new HashMap<>();
    public static WeakHashMap<ServerPlayerEntity, TeleportTarget> respawnOverwrite = new WeakHashMap<>();

    public static Mode getFor(ServerPlayerEntity player) {
        if (player instanceof DummyPlayer) return Mode.PLAY;
        return modes.getOrDefault(player.getUuid(), Mode.LOBBY);
    }

    static {
        ServerTickEvents.END_SERVER_TICK.register((srv) -> {
            for (UUID uuid : new HashSet<>(modes.keySet())) {
                ServerPlayerEntity player = FireFlow.server.getPlayerManager().getPlayer(uuid);
                if (player == null) modes.remove(uuid);
            }
        });
    }

    public static void move(ServerPlayerEntity player, Mode mode, Space space) {
        runOnWorld(player.getServerWorld(), () -> handleExit(player));
        if (player instanceof DummyPlayer) return;

        space = space != null ? space : SpaceManager.getSpaceForPlayer(player);
        if (space == null) mode = Mode.LOBBY;

        if (mode == Mode.LOBBY) {
            runOnWorld(Lobby.world, () -> {
                ServerPlayerEntity newPlayer = transfer(player, Lobby.world);
                modes.put(newPlayer.getUuid(), Mode.LOBBY);
                Lobby.onSpawn(newPlayer);
            });
            return;
        }

        if (mode == Mode.CODE) {
            Space lambdaSpace = space;
            runOnWorld(space.codeWorld, () -> {
                ServerPlayerEntity newPlayer = transfer(player, lambdaSpace.codeWorld);
                modes.put(newPlayer.getUuid(), Mode.CODE);
                lambdaSpace.editor.enterCode(EditOrigin.ofPlayer(newPlayer));
            });
            return;
        }

        if (mode == Mode.BUILD) {
            Space lambdaSpace = space;
            runOnWorld(space.playWorld, () -> {
                ServerPlayerEntity newPlayer = transfer(player, lambdaSpace.playWorld);
                modes.put(newPlayer.getUuid(), Mode.BUILD);
                lambdaSpace.enterBuild(newPlayer);
            });
            return;
        }

        Space lambdaSpace = space;
        runOnWorld(space.playWorld, () -> {
            ServerPlayerEntity newPlayer = transfer(player, lambdaSpace.playWorld);
            modes.put(newPlayer.getUuid(), Mode.PLAY);
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
            space.editor.exitCode(EditOrigin.ofPlayer(player));
        }
        if (mode == Mode.PLAY && space != null) {
            if (space.playWorld.thread == Thread.currentThread()) {
                space.evaluator.exitPlay(player);
            } else {
                space.playWorld.submit(() -> {
                    space.evaluator.exitPlay(player);
                });
            }
        }
    }

    private static ServerPlayerEntity transfer(ServerPlayerEntity player, ServerWorld world) {
        respawnOverwrite.put(player, new TeleportTarget(world, new Vec3d(0, 1, 0), Vec3d.ZERO, 0, 0, TeleportTarget.NO_OP));
        player = FireFlow.server.getPlayerManager().respawnPlayer(player, true, Entity.RemovalReason.CHANGED_DIMENSION);
        player = SetPlayerSkinNode.reset(player);
        player.networkHandler.player = player;
        Statistics.reset(player);
        return player;
    }

    public static void onJoinedServer(ServerPlayerEntity player) {
        modes.put(player.getUuid(), Mode.LOBBY);
    }

    public enum Mode {
        PLAY,
        BUILD,
        CODE,
        LOBBY
    }

}
