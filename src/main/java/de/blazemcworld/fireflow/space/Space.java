package de.blazemcworld.fireflow.space;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.code.CodeEditor;
import de.blazemcworld.fireflow.code.CodeEvaluator;
import de.blazemcworld.fireflow.code.CodeWorld;
import de.blazemcworld.fireflow.code.VariableStore;
import de.blazemcworld.fireflow.util.DummyPlayer;
import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Space {
    public final SpaceInfo info;
    public final PlayWorld playWorld;
    public final CodeWorld codeWorld;
    public final CodeEditor editor;
    public final VariableStore savedVariables;
    private int emptyTimer = 0;
    public CodeEvaluator evaluator;
    public final DummyManager dummyManager;

    public Space(SpaceInfo info) {
        this.info = info;
        playWorld = PlayWorld.create("play-" + info.id, this);
        codeWorld = CodeWorld.create("code-" + info.id, this);
        editor = new CodeEditor(this, codeWorld);
        savedVariables = new VariableStore();
        try {
            if (!Files.exists(path())) Files.createDirectories(path());
        } catch (IOException e) {
            FireFlow.LOGGER.error("Failed to create directory for space {}!", info.id, e);
        }

        if (Files.exists(path().resolve("variables.json"))) {
            try {
                savedVariables.load(JsonParser.parseString(Files.readString(path().resolve("variables.json"))).getAsJsonObject());
            } catch (IOException e) {
                FireFlow.LOGGER.error("Failed to load variables.json for space {}!", info.id, e);
            }
        }
        editor.load();
        evaluator = new CodeEvaluator(this);
        dummyManager = new DummyManager(this);
    }

    public void save() {
        JsonObject vars = savedVariables.toJson();
        try {
            Files.writeString(path().resolve("variables.json"), vars.toString());
        } catch (IOException e) {
            FireFlow.LOGGER.error("Failed to save variables.json for space {}!", info.id, e);
        }
        editor.save();
    }

    public boolean isInactive() {
        return emptyTimer > 100;
    }

    protected void unload(Runnable callback) {
        dummyManager.reset();
        for (ServerPlayerEntity player : new ArrayList<>(playWorld.getPlayers())) {
            ModeManager.move(player, ModeManager.Mode.LOBBY, this);
        }
        for (ServerPlayerEntity player : new ArrayList<>(codeWorld.getPlayers())) {
            ModeManager.move(player, ModeManager.Mode.LOBBY, this);
        }
        editor.close();

        try {
            codeWorld.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        evaluator.stop();
        playWorld.closeSoon(callback);
    }

    public Set<ServerPlayerEntity> getPlayers() {
        HashSet<ServerPlayerEntity> out = new HashSet<>();
        out.addAll(playWorld.getPlayers());
        out.addAll(codeWorld.getPlayers());
        out.removeIf(p -> p instanceof DummyPlayer);
        return out;
    }

    public void tick() {
        if (getPlayers().isEmpty()) {
            emptyTimer++;
        } else {
            emptyTimer = 0;
        }
    }

    public void enterPlay(ServerPlayerEntity player) {
        evaluator.onJoin(player);
    }

    public void enterBuild(ServerPlayerEntity player) {
        player.changeGameMode(GameMode.CREATIVE);
    }

    public Path path() {
        return Path.of("spaces").resolve(String.valueOf(info.id));
    }

    public void reload() {
        dummyManager.reset();
        for (ServerPlayerEntity player : new ArrayList<>(playWorld.getPlayers())) {
            if (info.isOwnerOrDeveloper(player.getUuid())) {
                ModeManager.move(player, ModeManager.Mode.CODE, this);
            } else {
                ModeManager.move(player, ModeManager.Mode.LOBBY, this);
            }
        }
        evaluator.stop();
        playWorld.getChunkManager().chunkLoadingManager.unloadChunks(() -> true);
        evaluator = new CodeEvaluator(this);
    }
}
