package de.blazemcworld.fireflow.command;

import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceManager;
import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;

public class CommandHelper {

    public static ServerPlayer getPlayer(CommandSourceStack src) {
        if (!src.isPlayer()) {
            src.sendFailure(Component.literal("You must be a player for this!"));
            return null;
        }
        return src.getPlayer();
    }

    public static Space getSpace(ServerPlayer player) {
        if (player == null) return null;
        Space space = SpaceManager.getSpaceForPlayer(player);
        if (space == null) {
            player.sendSystemMessage(Component.literal("You must be on a space for this!").withColor(TextColor.RED));
            return null;
        }
        return space;
    }

    public static boolean isOwner(ServerPlayer player, Space space) {
        if (space == null || player == null) return false;
        if (!space.info.owner.equals(player.getUUID())) {
            player.sendSystemMessage(Component.literal("You are not allowed to do that!").withColor(TextColor.RED));
            return false;
        }
        return true;
    }
    
    public static boolean isDeveloperOrOwner(ServerPlayer player, Space space) {
        if (space == null || player == null) return false;
        if (!space.info.isOwnerOrDeveloper(player.getUUID())) {
            player.sendSystemMessage(Component.literal("You are not allowed to do that!").withColor(TextColor.RED));
            return false;
        }
        return true;
    }

    public static boolean isInCode(ServerPlayer player, Space space) {
        if (space == null || player == null) return false;
        if (ModeManager.getFor(player) != ModeManager.Mode.CODE) {
            player.sendSystemMessage(Component.literal("You must be in code mode for this!").withColor(TextColor.RED));
            return false;
        }
        return true;
    }

}
