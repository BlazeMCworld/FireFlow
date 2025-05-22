package de.blazemcworld.fireflow;

import de.blazemcworld.fireflow.code.node.NodeList;
import de.blazemcworld.fireflow.code.type.AllTypes;
import de.blazemcworld.fireflow.code.web.WebServer;
import de.blazemcworld.fireflow.command.*;
import de.blazemcworld.fireflow.space.Lobby;
import de.blazemcworld.fireflow.space.PlayWorld;
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;
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
            TextWidth.init();
            SpaceManager.load();
            Lobby.init();
            NodeList.init();
            AllTypes.init();
            WebServer.init();

            server.getOverworld().getWorldBorder().setSize(512);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register((srv -> {
            WebServer.stop();
            for (ServerPlayerEntity player : new ArrayList<>(srv.getPlayerManager().getPlayerList())) {
                player.networkHandler.disconnect(Text.literal("Server stopped!"));
            }
            Set<World> worlds = new HashSet<>(srv.worlds.values());
            CountDownLatch counter = new CountDownLatch(worlds.size());
            for (World w : worlds) {
                if (w instanceof PlayWorld s) {
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
            handler.player.removeAllPassengers();
            handler.player.stopRiding();
        });

        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            if (world instanceof PlayWorld play) {
                play.submit(() -> play.space.evaluator.onChunkLoad(chunk.getPos().x, chunk.getPos().z));
            }
        });
        ServerChunkEvents.CHUNK_GENERATE.register((world, chunk) -> {
            if (world instanceof PlayWorld play) {
                play.submit(() -> play.space.evaluator.onChunkLoad(chunk.getPos().x, chunk.getPos().z));
            }
        });
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
            Space space = SpaceManager.getSpaceForWorld((ServerWorld) entity.getWorld());
            if (space != null && space.playWorld == entity.getWorld()) return space.evaluator.allowDeath(entity, source, amount);
            return true;
        });
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> amount >= 0);

        CommandRegistrationCallback.EVENT.register((cd, reg, env) -> {
            CodeCommand.register(cd);
            PlayCommand.register(cd);
            MonitorCommand.register(cd);
            LobbyCommand.register(cd);
            BuildCommand.register(cd);
            ReloadCommand.register(cd);
            AddNodeCommand.register(cd);
            ContributorCommand.register(cd);
            FunctionCommand.register(cd);
            LocateCommand.register(cd);
            ShowLagCommand.register(cd);
            DummyCommand.register(cd);
            VariablesCommand.register(cd);
            AuthWebCommand.register(cd);
            DebugCommand.register(cd);
        });
    }
}
