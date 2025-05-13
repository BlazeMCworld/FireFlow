package de.blazemcworld.fireflow.code;

import de.blazemcworld.fireflow.code.widget.WidgetVec;
import net.minecraft.server.network.ServerPlayerEntity;

public record CodeInteraction(ServerPlayerEntity player, WidgetVec pos, Type type, String message) {
    public enum Type {
        LEFT_CLICK, RIGHT_CLICK, SWAP_HANDS, CHAT
    }
}
