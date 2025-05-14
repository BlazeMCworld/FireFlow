package de.blazemcworld.fireflow.code.node.impl.player.effect;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.ProfileResult;
import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.TeleportTarget;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.WeakHashMap;

public class SetPlayerSkinNode extends Node {

    private static final WeakHashMap<GameProfile, Boolean> needsReset = new WeakHashMap<>();

    public SetPlayerSkinNode() {
        super("set_player_skin", "Set Player Skin", "Changes the displayed skin of a player", Items.LEATHER_HELMET);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<String> skin = new Input<>("skin", "Skin", StringType.INSTANCE);
        Input<String> mode = new Input<>("mode", "Mode", StringType.INSTANCE)
                .options("data", "name", "uuid", "reset");
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            String m = mode.getValue(ctx);
            switch (m) {
                case "data":
                    player.getValue(ctx).tryUse(ctx, p -> {
                        setSkin(p, List.of(new Property("textures", skin.getValue(ctx))));
                    });
                    break;

                case "name":
                case "uuid":
                    Thread.startVirtualThread(() -> {
                        GameProfile profile = null;
                        if (m.equals("name")) {
                            profile = FireFlow.server.getGameProfileRepo().findProfileByName(skin.getValue(ctx)).orElse(null);
                            if (profile == null) return;
                            ProfileResult result = FireFlow.server.getSessionService().fetchProfile(profile.getId(), true);
                            if (result == null) return;
                            profile = result.profile();
                        }
                        if (m.equals("uuid")) {
                            try {
                                ProfileResult result = FireFlow.server.getSessionService().fetchProfile(UUID.fromString(skin.getValue(ctx)), true);
                                if (result == null) return;
                                profile = result.profile();
                            } catch (IllegalArgumentException ignore) {
                            }
                        }

                        if (ctx.evaluator.isStopped()) return;
                        if (profile == null) return;
                        GameProfile resultingProfile = profile;
                        ctx.evaluator.nextTick(() -> {
                            player.getValue(ctx).tryUse(ctx, p -> {
                                setSkin(p, resultingProfile.getProperties().get("textures"));
                            });
                        });
                    });
                    break;

                case "reset":
                    player.getValue(ctx).tryUse(ctx, SetPlayerSkinNode::reset);
                    break;
            }
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new SetPlayerSkinNode();
    }

    private static ServerPlayerEntity setSkin(ServerPlayerEntity player, Collection<Property> textures) {
        synchronized (needsReset) {
            needsReset.put(player.getGameProfile(), true);
        }
        FireFlow.server.getPlayerManager().sendToAll(new PlayerRemoveS2CPacket(List.of(player.getUuid())));

        player.getGameProfile().getProperties().removeAll("textures");
        player.getGameProfile().getProperties().putAll("textures", textures);
        FireFlow.server.getPlayerManager().sendToAll(PlayerListS2CPacket.entryFromPlayer(List.of(player)));
        ModeManager.respawnOverwrite.put(player, new TeleportTarget(player.getServerWorld(), player.getPos(), player.getVelocity(), player.getYaw(), player.getPitch(), TeleportTarget.NO_OP));
        player = FireFlow.server.getPlayerManager().respawnPlayer(player, true, Entity.RemovalReason.DISCARDED);
        player.networkHandler.player = player;
        return player;
    }

    public static ServerPlayerEntity reset(ServerPlayerEntity player) {
        boolean needed = false;
        synchronized (needsReset) {
            if (needsReset.containsKey(player.getGameProfile())) {
                needed = true;
                needsReset.remove(player.getGameProfile());
            }
        }
        if (!needed) return player;
        ProfileResult result = FireFlow.server.getSessionService().fetchProfile(player.getUuid(), true);
        if (result == null) return player;
        player = setSkin(player, result.profile().getProperties().get("textures"));
        return player;
    }
}
