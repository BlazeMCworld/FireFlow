package de.blazemcworld.fireflow.code.node.impl.player.visual;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.TextType;
import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;

public class BroadcastNode extends Node {

    public BroadcastNode() {
        super("broadcast", "Broadcast", "Sends a message to all players on the space", Items.GOAT_HORN);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<Component> message = new Input<>("message", "Message", TextType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            Component msg = message.getValue(ctx);
            for (ServerPlayer player : ctx.evaluator.players()) {
                if (ModeManager.getFor(player) != ModeManager.Mode.PLAY) continue;
                player.sendSystemMessage(msg);
            }
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new BroadcastNode();
    }

}
