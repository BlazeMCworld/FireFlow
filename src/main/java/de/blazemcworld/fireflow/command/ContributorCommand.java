package de.blazemcworld.fireflow.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.messages.Messages;
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
                                Messages.sendMessage("There are no "+ id + "s!", Messages.INFO, player);
                                return Command.SINGLE_SUCCESS;
                            }

                            Messages.sendMessage("Space " + id + (contributors.size() == 1 ? "" : "s") + " (" + contributors.size() + "):", Messages.INFO, player);
                            for (UUID uuid : contributors) {
                                resolveName(player.getServerWorld(), uuid, name -> Messages.sendMessage(
                                        "<hover:show_text:\"<default>" + uuid.toString() + "\">" + name, Messages.FOLLOWUP, player));
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
                                        Messages.sendMessage("You are always a " + id + "!", Messages.ERROR, player);
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    resolveUUID(player.getServerWorld(), name, uuid -> {
                                        if (uuid == null) {
                                            Messages.sendMessage("Could not find player with name "+ name, Messages.ERROR, player);
                                            return;
                                        }

                                        Set<UUID> contributors = getMap.apply(space.info);
                                        if (contributors.contains(uuid)) {
                                            Messages.sendMessage("Player " + name + " is already a " + id, Messages.ERROR, player);
                                            return;
                                        }
                                        contributors.add(uuid);
                                        Messages.sendMessage("Added " + name + " as " + id, Messages.SUCCESS, player);
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
                                        Messages.sendMessage("You cannot remove yourself!", Messages.ERROR, player);
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    resolveUUID(player.getServerWorld(), name, uuid -> {
                                        if (uuid == null) {
                                            Messages.sendMessage("Could not find player with name " + name, Messages.ERROR, player);
                                            return;
                                        }

                                        Set<UUID> contributors = getMap.apply(space.info);
                                        if (!contributors.contains(uuid)) {
                                            Messages.sendMessage("Player " + name + " is not a " + id, Messages.ERROR, player);
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

                                        Messages.sendMessage("Removed " + name + " as " + id, Messages.SUCCESS, player);
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
