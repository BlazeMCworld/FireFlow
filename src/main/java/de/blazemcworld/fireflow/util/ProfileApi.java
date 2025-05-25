package de.blazemcworld.fireflow.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileResult;
import de.blazemcworld.fireflow.FireFlow;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ProfileApi {

    public static Optional<GameProfile> fromUUID(UUID uuid) {
        ProfileResult result = FireFlow.server.getSessionService().fetchProfile(uuid, false);
        if (result == null || result.profile() == null) return Optional.empty();
        return Optional.of(result.profile());
    }

    public static Optional<GameProfile> fromName(String name) {
        return FireFlow.server.getGameProfileRepo().findProfileByName(name);
    }

    public static String displayName(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<GameProfile> profile = fromUUID(uuid);
            return profile.map(GameProfile::getName).orElse("???");
        }).completeOnTimeout("???", 5, TimeUnit.MILLISECONDS).join();
    }
}
