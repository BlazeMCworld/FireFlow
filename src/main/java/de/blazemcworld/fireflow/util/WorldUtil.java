package de.blazemcworld.fireflow.util;

import de.blazemcworld.fireflow.FireFlow;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;

public class WorldUtil {

    public static void setGameRules(ServerWorld world) {
        GameRules rules = world.getGameRules();
        rules.get(GameRules.DO_DAYLIGHT_CYCLE).set(false, FireFlow.server);
        rules.get(GameRules.DO_WEATHER_CYCLE).set(false, FireFlow.server);
        rules.get(GameRules.DO_MOB_SPAWNING).set(false, FireFlow.server);
        rules.get(GameRules.SPAWN_CHUNK_RADIUS).set(0, FireFlow.server);
        rules.get(GameRules.SHOW_DEATH_MESSAGES).set(false, FireFlow.server);
        rules.get(GameRules.ANNOUNCE_ADVANCEMENTS).set(false, FireFlow.server);
        rules.get(GameRules.SPAWN_RADIUS).set(0, FireFlow.server);
    }

}
