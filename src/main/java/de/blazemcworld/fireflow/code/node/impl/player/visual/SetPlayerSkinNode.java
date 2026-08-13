package de.blazemcworld.fireflow.code.node.impl.player.visual;

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
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.portal.TeleportTransition;

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
                            com.mojang.authlib.yggdrasil.response.NameAndId info = FireFlow.server.services().profileRepository().findProfileByName(skin.getValue(ctx)).orElse(null);
                            if (info == null) return;
                            ProfileResult result = FireFlow.server.services().sessionService().fetchProfile(info.id(), true);
                            if (result == null) return;
                            profile = result.profile();
                        }
                        if (m.equals("uuid")) {
                            try {
                                ProfileResult result = FireFlow.server.services().sessionService().fetchProfile(UUID.fromString(skin.getValue(ctx)), true);
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
                                setSkin(p, resultingProfile.properties().get("textures"));
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

    private static ServerPlayer setSkin(ServerPlayer player, Collection<Property> textures) {
        synchronized (needsReset) {
            needsReset.put(player.getGameProfile(), true);
        }
        FireFlow.server.getPlayerList().broadcastAll(new ClientboundPlayerInfoRemovePacket(List.of(player.getUUID())));

        player.getGameProfile().properties().removeAll("textures");
        player.getGameProfile().properties().putAll("textures", textures);
        FireFlow.server.getPlayerList().broadcastAll(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(player)));
        ModeManager.respawnOverwrite.put(player, new TeleportTransition(player.level(), player.position(), player.getDeltaMovement(), player.getYRot(), player.getXRot(), TeleportTransition.DO_NOTHING));
        player = FireFlow.server.getPlayerList().respawn(player, true, Entity.RemovalReason.DISCARDED);
        player.connection.player = player;
        return player;
    }

    public static ServerPlayer reset(ServerPlayer player) {
        boolean needed = false;
        synchronized (needsReset) {
            if (needsReset.containsKey(player.getGameProfile())) {
                needed = true;
                needsReset.remove(player.getGameProfile());
            }
        }
        if (!needed) return player;
        ProfileResult result = FireFlow.server.services().sessionService().fetchProfile(player.getUUID(), true);
        if (result == null) return player;
        player = setSkin(player, result.profile().properties().get("textures"));
        return player;
    }
}
