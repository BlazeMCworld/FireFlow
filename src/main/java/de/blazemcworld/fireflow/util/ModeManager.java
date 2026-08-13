package de.blazemcworld.fireflow.util;

import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.code.EditOrigin;
import de.blazemcworld.fireflow.code.node.impl.player.visual.SetPlayerSkinNode;
import de.blazemcworld.fireflow.space.Lobby;
import de.blazemcworld.fireflow.space.PlayLevel;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.WeakHashMap;

public class ModeManager {

    private static final HashMap<UUID, Mode> modes = new HashMap<>();
    public static WeakHashMap<ServerPlayer, TeleportTransition> respawnOverwrite = new WeakHashMap<>();

    public static Mode getFor(ServerPlayer player) {
        if (player instanceof DummyPlayer) return Mode.PLAY;
        return modes.getOrDefault(player.getUUID(), Mode.LOBBY);
    }

    static {
        ServerTickEvents.END_SERVER_TICK.register((srv) -> {
            for (UUID uuid : new HashSet<>(modes.keySet())) {
                ServerPlayer player = FireFlow.server.getPlayerList().getPlayer(uuid);
                if (player == null) modes.remove(uuid);
            }
        });
    }

    public static void move(ServerPlayer player, Mode mode, Space space) {
        runOnLevel(player.level(), () -> handleExit(player));
        if (player instanceof DummyPlayer) return;
        if (!FireFlow.server.isRunning()) return;

        space = space != null ? space : SpaceManager.getSpaceForPlayer(player);
        if (space == null) mode = Mode.LOBBY;

        if (mode == Mode.LOBBY) {
            runOnLevel(Lobby.level, () -> {
                ServerPlayer newPlayer = transfer(player, Lobby.level);
                modes.put(newPlayer.getUUID(), Mode.LOBBY);
                Lobby.onSpawn(newPlayer);
            });
            return;
        }

        if (mode == Mode.CODE) {
            Space lambdaSpace = space;
            runOnLevel(space.codeLevel, () -> {
                ServerPlayer newPlayer = transfer(player, lambdaSpace.codeLevel);
                modes.put(newPlayer.getUUID(), Mode.CODE);
                lambdaSpace.editor.enterCode(EditOrigin.ofPlayer(newPlayer));
            });
            return;
        }

        if (mode == Mode.BUILD) {
            Space lambdaSpace = space;
            runOnLevel(space.playLevel, () -> {
                ServerPlayer newPlayer = transfer(player, lambdaSpace.playLevel);
                modes.put(newPlayer.getUUID(), Mode.BUILD);
                lambdaSpace.enterBuild(newPlayer);
            });
            return;
        }

        Space lambdaSpace = space;
        runOnLevel(space.playLevel, () -> {
            ServerPlayer newPlayer = transfer(player, lambdaSpace.playLevel);
            modes.put(newPlayer.getUUID(), Mode.PLAY);
            lambdaSpace.enterPlay(newPlayer);
        });
    }

    private static void runOnLevel(ServerLevel level, Runnable task) {
        if (level.thread != Thread.currentThread()) {
            if (level instanceof PlayLevel s) {
                s.submit(task);
                return;
            }
            FireFlow.server.execute(task);
            return;
        }
        task.run();
    }

    public static void handleExit(ServerPlayer player) {
        Space space = SpaceManager.getSpaceForPlayer(player);
        Mode mode = getFor(player);

        if (mode == Mode.CODE && space != null) {
            space.editor.exitCode(EditOrigin.ofPlayer(player));
        }
        if (mode == Mode.PLAY && space != null) {
            if (space.playLevel.thread == Thread.currentThread()) {
                space.evaluator.exitPlay(player);
            } else {
                space.playLevel.submit(() -> {
                    space.evaluator.exitPlay(player);
                });
            }
        }
    }

    private static ServerPlayer transfer(ServerPlayer player, ServerLevel world) {
        respawnOverwrite.put(player, new TeleportTransition(world, new Vec3(0, 1, 0), Vec3.ZERO, 0, 0, TeleportTransition.DO_NOTHING));
        player = FireFlow.server.getPlayerList().respawn(player, true, Entity.RemovalReason.CHANGED_DIMENSION);
        player = SetPlayerSkinNode.reset(player);
        player.connection.player = player;
        Statistics.reset(player);
        return player;
    }

    public static void onJoinedServer(ServerPlayer player) {
        modes.put(player.getUUID(), Mode.LOBBY);
    }

    public enum Mode {
        PLAY,
        BUILD,
        CODE,
        LOBBY
    }

}
