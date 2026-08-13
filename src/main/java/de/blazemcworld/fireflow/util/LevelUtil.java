package de.blazemcworld.fireflow.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gamerules.GameRules;

public class LevelUtil {

    public static void setGameRules(ServerLevel world) {
        net.minecraft.world.level.gamerules.GameRules rules = world.getGameRules();
        rules.set(GameRules.ADVANCE_TIME, false, null);
        rules.set(GameRules.ADVANCE_WEATHER, false, null);
        rules.set(GameRules.SPAWN_MOBS, false, null);
        rules.set(GameRules.SHOW_DEATH_MESSAGES, false, null);
        rules.set(GameRules.SHOW_ADVANCEMENT_MESSAGES, false, null);
        rules.set(GameRules.RESPAWN_RADIUS, 0, null);
    }

}
