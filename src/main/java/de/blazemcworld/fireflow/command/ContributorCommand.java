package de.blazemcworld.fireflow.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.space.PlayWorld;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceInfo;
import de.blazemcworld.fireflow.space.SpaceManager;
import de.blazemcworld.fireflow.util.ModeManager;
import de.blazemcworld.fireflow.util.ProfileApi;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class ContributorCommand {


    public static void attach(LiteralArgumentBuilder<ServerCommandSource> node) {
        attach(node, "builder", info -> info.builders);
        attach(node, "developer", info -> info.developers);
    }

    private static void attach(LiteralArgumentBuilder<ServerCommandSource> node, String id, Function<SpaceInfo, Set<UUID>> getMap) {
        node.then(CommandManager.literal(id)
                .then(CommandManager.literal("list")
                        .executes(ctx -> {
                            ServerPlayerEntity player = CommandHelper.getPlayer(ctx.getSource());
                            Space space = CommandHelper.getSpace(player);
                            if (!CommandHelper.isOwner(player, space)) return Command.SINGLE_SUCCESS;

                            Set<UUID> contributors = getMap.apply(space.info);

                            if (contributors.isEmpty()) {
                                player.sendMessage(Text.literal("There are no " + id + "s!").formatted(Formatting.RED));
                                return Command.SINGLE_SUCCESS;
                            }

                            player.sendMessage(Text.literal("Space " + id + (contributors.size() == 1 ? "" : "s") + " (" + contributors.size() + "):").formatted(Formatting.AQUA));
                            for (UUID uuid : contributors) {
                                resolveName(player.getServerWorld(), uuid, name -> player.sendMessage(Text.literal("- " + name).formatted(Formatting.DARK_AQUA)));
                            }

                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(CommandManager.literal("add")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    for (ServerPlayerEntity player : FireFlow.server.getPlayerManager().getPlayerList()) {
                                        builder.suggest(player.getGameProfile().getName());
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    ServerPlayerEntity player = CommandHelper.getPlayer(ctx.getSource());
                                    Space space = CommandHelper.getSpace(player);
                                    if (!CommandHelper.isOwner(player, space)) return Command.SINGLE_SUCCESS;

                                    String name = ctx.getArgument("name", String.class);

                                    if (player.getGameProfile().getName().equalsIgnoreCase(name)) {
                                        player.sendMessage(Text.literal("You are always a " + id + "!").formatted(Formatting.RED));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    resolveUUID(player.getServerWorld(), name, uuid -> {
                                        if (uuid == null) {
                                            player.sendMessage(Text.literal("Could not find player with name " + name).formatted(Formatting.RED));
                                            return;
                                        }

                                        Set<UUID> contributors = getMap.apply(space.info);
                                        if (contributors.contains(uuid)) {
                                            player.sendMessage(Text.literal("Player " + name + " is already a " + id).formatted(Formatting.RED));
                                            return;
                                        }
                                        contributors.add(uuid);
                                        player.sendMessage(Text.literal("Added " + name + " as " + id).formatted(Formatting.AQUA));
                                    });

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(CommandManager.literal("remove")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    for (ServerPlayerEntity player : FireFlow.server.getPlayerManager().getPlayerList()) {
                                        builder.suggest(player.getGameProfile().getName());
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    ServerPlayerEntity player = CommandHelper.getPlayer(ctx.getSource());
                                    Space space = CommandHelper.getSpace(player);
                                    if (!CommandHelper.isOwner(player, space)) return Command.SINGLE_SUCCESS;

                                    String name = ctx.getArgument("name", String.class);

                                    if (player.getGameProfile().getName().equalsIgnoreCase(name)) {
                                        player.sendMessage(Text.literal("You cannot remove yourself!").formatted(Formatting.RED));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    resolveUUID(player.getServerWorld(), name, uuid -> {
                                        if (uuid == null) {
                                            player.sendMessage(Text.literal("Could not find player with name " + name).formatted(Formatting.RED));
                                            return;
                                        }

                                        Set<UUID> contributors = getMap.apply(space.info);
                                        if (!contributors.contains(uuid)) {
                                            player.sendMessage(Text.literal("Player " + name + " is not a " + id).formatted(Formatting.RED));
                                            return;
                                        }
                                        contributors.remove(uuid);

                                        ServerPlayerEntity target = FireFlow.server.getPlayerManager().getPlayer(uuid);
                                        if (target != null && SpaceManager.getSpaceForPlayer(target) == space) {
                                            ModeManager.Mode mode = ModeManager.getFor(target);
                                            if (id.equals("builder") && mode == ModeManager.Mode.BUILD) {
                                                ModeManager.move(target, ModeManager.Mode.LOBBY, space);
                                            }
                                            if (id.equals("developer") && mode == ModeManager.Mode.CODE) {
                                                ModeManager.move(target, ModeManager.Mode.LOBBY, space);
                                            }
                                        }

                                        player.sendMessage(Text.literal("Removed " + name + " as " + id).formatted(Formatting.AQUA));
                                    });

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
        );
    }

    private static void resolveName(ServerWorld world, UUID uuid, Consumer<String> callback) {
        Thread.startVirtualThread(() -> {
            String name = ProfileApi.fromUUID(uuid).map(GameProfile::getName).orElse("<" + uuid + ">");
            if (world instanceof PlayWorld play) {
                play.submit(() -> callback.accept(name));
                return;
            }
            FireFlow.server.execute(() -> callback.accept(name));
        });
    }

    private static void resolveUUID(ServerWorld world, String name, Consumer<UUID> callback) {
        Thread.startVirtualThread(() -> {
            UUID uuid = ProfileApi.fromName(name).map(GameProfile::getId).orElse(null);

            if (world instanceof PlayWorld play) {
                play.submit(() -> callback.accept(uuid));
                return;
            }
            FireFlow.server.execute(() -> callback.accept(uuid));
        });
    }
}
