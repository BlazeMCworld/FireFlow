package de.blazemcworld.fireflow.command;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.response.NameAndId;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.space.PlayLevel;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceInfo;
import de.blazemcworld.fireflow.space.SpaceManager;
import de.blazemcworld.fireflow.util.ModeManager;
import de.blazemcworld.fireflow.util.ProfileApi;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class ContributorCommand {


    public static void attach(LiteralArgumentBuilder<CommandSourceStack> node) {
        attach(node, "builder", info -> info.builders);
        attach(node, "developer", info -> info.developers);
    }

    private static void attach(LiteralArgumentBuilder<CommandSourceStack> node, String id, Function<SpaceInfo, Set<UUID>> getMap) {
        node.then(Commands.literal(id)
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            ServerPlayer player = CommandHelper.getPlayer(ctx.getSource());
                            Space space = CommandHelper.getSpace(player);
                            if (!CommandHelper.isOwner(player, space)) return Command.SINGLE_SUCCESS;

                            Set<UUID> contributors = getMap.apply(space.info);

                            if (contributors.isEmpty()) {
                                player.sendSystemMessage(Component.literal("There are no " + id + "s!").withColor(TextColor.RED));
                                return Command.SINGLE_SUCCESS;
                            }

                            player.sendSystemMessage(Component.literal("Space " + id + (contributors.size() == 1 ? "" : "s") + " (" + contributors.size() + "):").withColor(TextColor.AQUA));
                            for (UUID uuid : contributors) {
                                resolveName(player.level(), uuid, name -> player.sendSystemMessage(Component.literal("- " + name).withColor(TextColor.DARK_AQUA)));
                            }

                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("add")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    for (ServerPlayer player : FireFlow.server.getPlayerList().getPlayers()) {
                                        builder.suggest(player.getGameProfile().name());
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    ServerPlayer player = CommandHelper.getPlayer(ctx.getSource());
                                    Space space = CommandHelper.getSpace(player);
                                    if (!CommandHelper.isOwner(player, space)) return Command.SINGLE_SUCCESS;

                                    String name = ctx.getArgument("name", String.class);

                                    if (player.getGameProfile().name().equalsIgnoreCase(name)) {
                                        player.sendSystemMessage(Component.literal("You are always a " + id + "!").withColor(TextColor.RED));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    resolveUUID(player.level(), name, uuid -> {
                                        if (uuid == null) {
                                            player.sendSystemMessage(Component.literal("Could not find player with name " + name).withColor(TextColor.RED));
                                            return;
                                        }

                                        Set<UUID> contributors = getMap.apply(space.info);
                                        if (contributors.contains(uuid)) {
                                            player.sendSystemMessage(Component.literal("Player " + name + " is already a " + id).withColor(TextColor.RED));
                                            return;
                                        }
                                        contributors.add(uuid);
                                        player.sendSystemMessage(Component.literal("Added " + name + " as " + id).withColor(TextColor.AQUA));
                                    });

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("remove")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    for (ServerPlayer player : FireFlow.server.getPlayerList().getPlayers()) {
                                        builder.suggest(player.getGameProfile().name());
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    ServerPlayer player = CommandHelper.getPlayer(ctx.getSource());
                                    Space space = CommandHelper.getSpace(player);
                                    if (!CommandHelper.isOwner(player, space)) return Command.SINGLE_SUCCESS;

                                    String name = ctx.getArgument("name", String.class);

                                    if (player.getGameProfile().name().equalsIgnoreCase(name)) {
                                        player.sendSystemMessage(Component.literal("You cannot remove yourself!").withColor(TextColor.RED));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    resolveUUID(player.level(), name, uuid -> {
                                        if (uuid == null) {
                                            player.sendSystemMessage(Component.literal("Could not find player with name " + name).withColor(TextColor.RED));
                                            return;
                                        }

                                        Set<UUID> contributors = getMap.apply(space.info);
                                        if (!contributors.contains(uuid)) {
                                            player.sendSystemMessage(Component.literal("Player " + name + " is not a " + id).withColor(TextColor.RED));
                                            return;
                                        }
                                        contributors.remove(uuid);

                                        ServerPlayer target = FireFlow.server.getPlayerList().getPlayer(uuid);
                                        if (target != null && SpaceManager.getSpaceForPlayer(target) == space) {
                                            ModeManager.Mode mode = ModeManager.getFor(target);
                                            if (id.equals("builder") && mode == ModeManager.Mode.BUILD) {
                                                ModeManager.move(target, ModeManager.Mode.LOBBY, space);
                                            }
                                            if (id.equals("developer") && mode == ModeManager.Mode.CODE) {
                                                ModeManager.move(target, ModeManager.Mode.LOBBY, space);
                                            }
                                        }

                                        player.sendSystemMessage(Component.literal("Removed " + name + " as " + id).withColor(TextColor.AQUA));
                                    });

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
        );
    }

    private static void resolveName(ServerLevel level, UUID uuid, Consumer<String> callback) {
        Thread.startVirtualThread(() -> {
            String name = ProfileApi.fromUUID(uuid).map(GameProfile::name).orElse("<" + uuid + ">");
            if (level instanceof PlayLevel play) {
                play.submit(() -> callback.accept(name));
                return;
            }
            FireFlow.server.execute(() -> callback.accept(name));
        });
    }

    private static void resolveUUID(ServerLevel level, String name, Consumer<UUID> callback) {
        Thread.startVirtualThread(() -> {
            UUID uuid = ProfileApi.fromName(name).map(NameAndId::id).orElse(null);

            if (level instanceof PlayLevel play) {
                play.submit(() -> callback.accept(uuid));
                return;
            }
            FireFlow.server.execute(() -> callback.accept(uuid));
        });
    }
}
