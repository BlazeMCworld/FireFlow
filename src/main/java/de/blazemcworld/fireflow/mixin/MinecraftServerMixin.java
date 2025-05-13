package de.blazemcworld.fireflow.mixin;

import de.blazemcworld.fireflow.space.PlayWorld;
import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Redirect(method = "createWorlds", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/Registry;getEntrySet()Ljava/util/Set;"))
    public Set<Map.Entry<RegistryKey<?>, ?>> disableBonusDimensions(Registry<?> instance) {
        return Set.of();
    }

    @Redirect(method = "runOneTask", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getWorlds()Ljava/lang/Iterable;"))
    public Iterable<ServerWorld> hidePlayWorlds(MinecraftServer instance) {
        List<ServerWorld> out = new ArrayList<>();
        for (ServerWorld w : instance.getWorlds()) {
            if (w instanceof PlayWorld) continue;
            out.add(w);
        }
        return out;
    }

    @Redirect(method = "loadWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;prepareStartRegion(Lnet/minecraft/server/WorldGenerationProgressListener;)V"))
    public void ignoreStartRegion(MinecraftServer instance, WorldGenerationProgressListener listener) {
    }

    @Redirect(method = "tickWorlds", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;getPlayerList()Ljava/util/List;"))
    private List<ServerPlayerEntity> hidePlayingPlayers(PlayerManager instance) {
        List<ServerPlayerEntity> out = new ArrayList<>(instance.getPlayerList());
        out.removeIf(p -> {
            if (!(p.getServerWorld() instanceof PlayWorld play)) return false;
            if (play.lastTick + 5000 < System.currentTimeMillis()) {
                ModeManager.move(p, ModeManager.Mode.LOBBY, play.space);
            }
            return true;
        });
        return out;
    }

    @Redirect(method = "tickWorlds", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getWorlds()Ljava/lang/Iterable;"))
    private Iterable<ServerWorld> copyWorldList(MinecraftServer instance) {
        return new ArrayList<>(instance.worlds.values());
    }

}
