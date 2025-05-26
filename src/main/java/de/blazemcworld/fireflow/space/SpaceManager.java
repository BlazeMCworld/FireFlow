package de.blazemcworld.fireflow.space;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.code.CodeWorld;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SpaceManager {

    private static final HashMap<Integer, Space> spaces = new HashMap<>();
    public static final HashMap<Integer, SpaceInfo> info = new HashMap<>();
    public static int lastId = 0;

    private static void save(boolean stop) {
        try {
            JsonObject data = new JsonObject();
            JsonObject spaces = new JsonObject();
            for (SpaceInfo spaceInfo : info.values()) {
                JsonObject space = new JsonObject();
                space.addProperty("name", spaceInfo.name);
                space.addProperty("icon", Registries.ITEM.getId(spaceInfo.icon).toString());
                space.addProperty("owner", spaceInfo.owner.toString());
                JsonArray developers = new JsonArray();
                for (UUID contributor : spaceInfo.developers) {
                    developers.add(contributor.toString());
                }
                space.add("developers", developers);
                JsonArray builders = new JsonArray();
                for (UUID contributor : spaceInfo.builders) {
                    builders.add(contributor.toString());
                }
                space.add("builders", builders);
                spaces.add(String.valueOf(spaceInfo.id), space);
            }
            data.add("spaces", spaces);
            data.addProperty("lastId", lastId);
            
            Files.writeString(Path.of("spaces.json"), data.toString());
        } catch (IOException e) {
            FireFlow.LOGGER.error("Failed to save spaces.json!", e);
        }

        for (Space space : getLoadedSpaces()) {
            space.save();
            if (space.isInactive() || stop) unloadSpace(space, null);
        }
    }

    public static void unloadSpace(Space space, Runnable callback) {
        synchronized (spaces) {
            spaces.remove(space.info.id);
        }
        space.unload(callback);
        FireFlow.LOGGER.info("Unloading space {}", space.info.id);
    }

    public static void load() {
        ServerLifecycleEvents.SERVER_STOPPING.register((srv) -> {
            save(true);
        });

        int[] nextSave = {100};
        ServerTickEvents.END_SERVER_TICK.register((srv) -> {
            nextSave[0]--;
            for (Space s : spaces.values()) s.tick();
            if (nextSave[0] > 0) return;
            nextSave[0] = 100;
            save(false);
        });

        try {
            if (!Files.exists(Path.of("spaces.json"))) return;

            JsonObject data = JsonParser.parseString(Files.readString(Path.of("spaces.json"))).getAsJsonObject();

            lastId = data.get("lastId").getAsInt();
            JsonObject spaces = data.getAsJsonObject("spaces");
            for (Map.Entry<String, JsonElement> raw : spaces.entrySet()) {
                JsonObject space = raw.getValue().getAsJsonObject();
                SpaceInfo spaceInfo = new SpaceInfo(Integer.parseInt(raw.getKey()));
                spaceInfo.name = space.get("name").getAsString();
                spaceInfo.icon = Registries.ITEM.get(Identifier.of(space.get("icon").getAsString()));
                spaceInfo.owner = UUID.fromString(space.get("owner").getAsString());
                spaceInfo.developers = new HashSet<>();
                spaceInfo.builders = new HashSet<>();
                for (JsonElement dev : space.has("contributors") ? space.getAsJsonArray("contributors") : space.get("developers").getAsJsonArray()) {
                    spaceInfo.developers.add(UUID.fromString(dev.getAsString()));
                }
                if (space.has("builders")) {
                    for (JsonElement builder : space.getAsJsonArray("builders")) {
                        spaceInfo.builders.add(UUID.fromString(builder.getAsString()));
                    }
                }
                info.put(spaceInfo.id, spaceInfo);
            }
        } catch (IOException e) {
            FireFlow.LOGGER.error("Failed to load spaces.json!", e);
        }
    }

    public static Space getOrLoadSpace(SpaceInfo info) {
       Space space;
        synchronized (spaces) {
            space = spaces.get(info.id);
        }
        if (space == null) {
            FireFlow.LOGGER.info("Loading space {}", info.id);
            space = new Space(info);
            synchronized (spaces) {
                spaces.put(info.id, space);
            }
        }
        return space;
    }

    public static Space getSpaceForPlayer(ServerPlayerEntity player) {
        return getSpaceForWorld(player.getServerWorld());
    }

    public static Space getSpaceForWorld(ServerWorld world) {
        if (world instanceof PlayWorld p) return p.space;
        if (world instanceof CodeWorld c) return c.space;
        return null;
    }

    public static List<Space> activeSpaces() {
        List<Space> out = new ArrayList<>();
        synchronized (spaces) {
            for (Space s : spaces.values()) {
                if (!s.getPlayers().isEmpty()) out.add(s);
            }
        }
        return out;
    }

    public static List<SpaceInfo> getOwnedSpaces(ServerPlayerEntity player) {
        List<SpaceInfo> out = new ArrayList<>();
        for (SpaceInfo i : info.values()) {
            if (i.owner.equals(player.getUuid())) out.add(i);
        }
        return out;
    }

    public static Space getIfLoaded(SpaceInfo info) {
        synchronized (spaces) {
            return spaces.get(info.id);
        }
    }

    public static SpaceInfo getInfo(int id) {
        return info.get(id);
    }

    public static List<Space> getLoadedSpaces() {
        synchronized (spaces) {
            return new ArrayList<>(spaces.values());
        }
    }

    public static void delete(Space space) {
        unloadSpace(space, () -> Thread.startVirtualThread(() -> {
            try {
                FileUtils.deleteDirectory(space.path().toFile());

                FileUtils.deleteDirectory(FireFlow.server.session.getWorldDirectory(space.playWorld.getRegistryKey()).toFile());
                FileUtils.deleteDirectory(FireFlow.server.session.getWorldDirectory(space.codeWorld.getRegistryKey()).toFile());

                info.remove(space.info.id);
            } catch (IOException e) {
                FireFlow.LOGGER.error("Failed to delete space {}!", space.info.id, e);
            }
        }));
    }
}
