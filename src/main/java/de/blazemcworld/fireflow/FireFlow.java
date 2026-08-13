package de.blazemcworld.fireflow;

import de.blazemcworld.fireflow.code.node.NodeList;
import de.blazemcworld.fireflow.code.type.AllTypes;
import de.blazemcworld.fireflow.code.web.WebServer;
import de.blazemcworld.fireflow.command.*;
import de.blazemcworld.fireflow.space.Lobby;
import de.blazemcworld.fireflow.space.PlayLevel;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceManager;
import de.blazemcworld.fireflow.util.ModeManager;
import de.blazemcworld.fireflow.util.Statistics;
import de.blazemcworld.fireflow.util.TextWidth;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class FireFlow implements ModInitializer {

    public static MinecraftServer server;
    public static final Logger LOGGER = LogManager.getLogger("FireFlow");

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            FireFlow.server = server;
            NodeList.init();

            if (String.valueOf(System.getenv("FIREFLOW_GENERATE_WIKI")).equalsIgnoreCase("true")) {
                WikiGenerator.generate();
                Runtime.getRuntime().halt(0);
            }

            TextWidth.init();
            SpaceManager.load();
            Lobby.init();
            AllTypes.init();
            WebServer.init();

            server.overworld().getWorldBorder().setSize(1024);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register((srv -> {
            WebServer.stop();
            for (ServerPlayer player : new ArrayList<>(srv.getPlayerList().getPlayers())) {
                player.connection.disconnect(Component.literal("Server stopped!"));
            }
            Set<ServerLevel> worlds = new HashSet<>(srv.levels.values());
            CountDownLatch counter = new CountDownLatch(worlds.size());
            for (ServerLevel w : worlds) {
                if (w instanceof PlayLevel s) {
                    s.closeSoon(counter::countDown);
                    continue;
                }
                counter.countDown();
            }
            try {
                counter.await();
            } catch (InterruptedException e) {
                FireFlow.LOGGER.error("Unexpected interrupt!", e);
            }
        }));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server1) -> {
            Statistics.reset(handler.player);
            ModeManager.onJoinedServer(handler.player);
            Lobby.onSpawn(handler.player);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ModeManager.handleExit(handler.player);
            handler.player.ejectPassengers();
            handler.player.stopRiding();
        });

        ServerChunkEvents.CHUNK_LOAD.register((level, chunk, isNew) -> {
            if (level instanceof PlayLevel play) {
                play.submit(() -> play.space.evaluator.onChunkLoad(chunk.getPos().x(), chunk.getPos().z()));
            }
        });
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
            Space space = SpaceManager.getSpaceForLevel((ServerLevel) entity.level());
            if (space != null && space.playLevel == entity.level()) return space.evaluator.allowDeath(entity, source, amount);
            return true;
        });
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> amount >= 0);

        CommandRegistrationCallback.EVENT.register((cd, reg, env) -> {
            CodeCommand.register(cd);
            PlayCommand.register(cd);
            LobbyCommand.register(cd);
            BuildCommand.register(cd);
            ReloadCommand.register(cd);
            AddNodeCommand.register(cd);
            FunctionCommand.register(cd);
            LocateCommand.register(cd);
            ShowLagCommand.register(cd);
            DummyCommand.register(cd);
            AuthWebCommand.register(cd);
            DebugCommand.register(cd);
            SpaceCommand.register(cd);
        });
    }
}
