package de.blazemcworld.fireflow.command;

import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceManager;
import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class CommandHelper {

    public static ServerPlayerEntity getPlayer(ServerCommandSource src) {
        if (!src.isExecutedByPlayer()) {
            src.sendError(Text.literal("You must be a player for this!"));
            return null;
        }
        return src.getPlayer();
    }

    public static Space getSpace(ServerPlayerEntity player) {
        if (player == null) return null;
        Space space = SpaceManager.getSpaceForPlayer(player);
        if (space == null) {
            player.sendMessage(Text.literal("You must be on a space for this!").formatted(Formatting.RED));
            return null;
        }
        return space;
    }

    public static boolean isOwner(ServerPlayerEntity player, Space space) {
        if (space == null || player == null) return false;
        if (!space.info.owner.equals(player.getUuid())) {
            player.sendMessage(Text.literal("You are not allowed to do that!").formatted(Formatting.RED));
            return false;
        }
        return true;
    }
    
    public static boolean isDeveloperOrOwner(ServerPlayerEntity player, Space space) {
        if (space == null || player == null) return false;
        if (!space.info.isOwnerOrDeveloper(player.getUuid())) {
            player.sendMessage(Text.literal("You are not allowed to do that!").formatted(Formatting.RED));
            return false;
        }
        return true;
    }

    public static boolean isInCode(ServerPlayerEntity player, Space space) {
        if (space == null || player == null) return false;
        if (ModeManager.getFor(player) != ModeManager.Mode.CODE) {
            player.sendMessage(Text.literal("You must be in code mode for this!").formatted(Formatting.RED));
            return false;
        }
        return true;
    }

}
