package de.blazemcworld.fireflow.mixin;

import de.blazemcworld.fireflow.space.PlayLevel;
import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Redirect(method = "createLevels", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;entrySet()Ljava/util/Set;"))
    public Set<Map.Entry<ResourceKey<?>, ?>> fireflow$disableBonusDimensions(Registry<?> instance) {
        return Set.of();
    }

    @Redirect(method = "pollTaskInternal", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getAllLevels()Ljava/lang/Iterable;"))
    public Iterable<ServerLevel> fireflow$hidePlayWorlds(MinecraftServer instance) {
        List<ServerLevel> out = new ArrayList<>();
        for (ServerLevel w : instance.getAllLevels()) {
            if (w instanceof PlayLevel) continue;
            out.add(w);
        }
        return out;
    }

    @Redirect(method = "loadLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;prepareLevels()V"))
    public void fireflow$ignoreStartRegion(MinecraftServer instance) {
    }

    @Redirect(method = "tickChildren", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;getPlayers()Ljava/util/List;"))
    private List<ServerPlayer> fireflow$hidePlayingPlayers(PlayerList instance) {
        List<ServerPlayer> out = new ArrayList<>(instance.getPlayers());
        out.removeIf(p -> {
            if (!(p.level() instanceof PlayLevel play)) return false;
            if (play.lastTick + 5000 < System.currentTimeMillis()) {
                ModeManager.move(p, ModeManager.Mode.LOBBY, play.space);
            }
            return true;
        });
        return out;
    }

    @Redirect(method = "tickChildren", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getAllLevels()Ljava/lang/Iterable;"))
    private Iterable<ServerLevel> fireflow$copyWorldList(MinecraftServer instance) {
        return new ArrayList<>(instance.levels.values());
    }

}
