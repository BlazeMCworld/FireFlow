package de.blazemcworld.fireflow.space;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.code.CodeEditor;
import de.blazemcworld.fireflow.code.CodeEvaluator;
import de.blazemcworld.fireflow.code.CodeLevel;
import de.blazemcworld.fireflow.code.VariableStore;
import de.blazemcworld.fireflow.util.DummyPlayer;
import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Space {
    public final SpaceInfo info;
    public final PlayLevel playLevel;
    public final CodeLevel codeLevel;
    public final CodeEditor editor;
    public final VariableStore savedVariables;
    private int emptyTimer = 0;
    public CodeEvaluator evaluator;
    public final DummyManager dummyManager;

    public Space(SpaceInfo info) {
        this.info = info;
        playLevel = PlayLevel.create("play-" + info.id, this);
        codeLevel = CodeLevel.create("code-" + info.id, this);
        editor = new CodeEditor(this, codeLevel);
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
        for (ServerPlayer player : new ArrayList<>(playLevel.players())) {
            ModeManager.move(player, ModeManager.Mode.LOBBY, this);
        }
        for (ServerPlayer player : new ArrayList<>(codeLevel.players())) {
            ModeManager.move(player, ModeManager.Mode.LOBBY, this);
        }
        editor.close();

        try {
            codeLevel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        evaluator.stop();
        playLevel.closeSoon(callback);
    }

    public Set<ServerPlayer> playersAnyMode() {
        HashSet<ServerPlayer> out = new HashSet<>();
        out.addAll(playLevel.players());
        out.addAll(codeLevel.players());
        out.removeIf(p -> p instanceof DummyPlayer);
        return out;
    }

    public Set<ServerPlayer> playersPlayMode() {
        HashSet<ServerPlayer> out = new HashSet<>();
        for (ServerPlayer player : playersAnyMode()) {
            if (ModeManager.getFor(player) == ModeManager.Mode.PLAY) {
                out.add(player);
            }
        }
        return out;
    }

    public void tick() {
        if (playersAnyMode().isEmpty()) {
            emptyTimer++;
        } else {
            emptyTimer = 0;
        }
    }

    public void enterPlay(ServerPlayer player) {
        evaluator.onJoin(player);
    }

    public void enterBuild(ServerPlayer player) {
        player.setGameMode(GameType.CREATIVE);
    }

    public Path path() {
        return Path.of("spaces").resolve(String.valueOf(info.id));
    }

    public void reload() {
        dummyManager.reset();
        for (ServerPlayer player : new ArrayList<>(playLevel.players())) {
            if (info.isOwnerOrDeveloper(player.getUUID())) {
                ModeManager.move(player, ModeManager.Mode.CODE, this);
            } else {
                ModeManager.move(player, ModeManager.Mode.LOBBY, this);
            }
        }
        evaluator.stop();
        playLevel.getChunkSource().chunkMap.processUnloads(() -> true);
        evaluator = new CodeEvaluator(this);
    }
}
