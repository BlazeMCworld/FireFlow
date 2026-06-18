package de.blazemcworld.fireflow.space;

import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.*;

public class BossBarManager {
    private final Map<UUID, List<ServerBossBar>> bossBars = new HashMap<>();

    public void setBossBarAtIndex(ServerPlayerEntity player, int index, ServerBossBar newBossBar) {
        List<ServerBossBar> bars = bossBars.computeIfAbsent(player.getUuid(), k -> new ArrayList<>());

        while (bars.size() <= index) {
            bars.add(null);
        }

        ServerBossBar existing = bars.get(index);
        if (existing != null) {
            existing.setName(newBossBar.getName());
            existing.setPercent(newBossBar.getPercent());
            existing.setColor(newBossBar.getColor());
            existing.setStyle(newBossBar.getStyle());
            existing.setDarkenSky(newBossBar.shouldDarkenSky());
            existing.setThickenFog(newBossBar.shouldThickenFog());
            existing.addPlayer(player);
        } else {
            bars.set(index, newBossBar);
            for (ServerBossBar bar : bars) {
                if(bar == null) continue;
                bar.removePlayer(player);
                bar.addPlayer(player);
            }
        }
    }



    public void removeBossBarAtIndex(ServerPlayerEntity player, int index) {
        List<ServerBossBar> bars = getBossBars(player);
        if (bars != null && index >= 0 && index < bars.size()) {
            ServerBossBar bar = bars.get(index);
            bar.removePlayer(player);
            bars.remove(index);
        }
    }

    public void clearBossBars(ServerPlayerEntity player) {
        for (ServerBossBar bar : getBossBars(player)) {
            if(bar == null) continue;
            bar.removePlayer(player);
        }
        bossBars.remove(player.getUuid());
    }

    public List<ServerBossBar> getBossBars(ServerPlayerEntity player) {
        return bossBars.getOrDefault(player.getUuid(), Collections.emptyList());
    }

    public void reset() {
        bossBars.clear();
    }
}
