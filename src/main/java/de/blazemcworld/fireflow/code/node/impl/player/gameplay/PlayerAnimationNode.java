package de.blazemcworld.fireflow.code.node.impl.player.gameplay;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;

public class PlayerAnimationNode extends Node {

    public PlayerAnimationNode() {
        super("player_animation", "Player Animation", "Makes the player do an animation", Items.GLOWSTONE_DUST);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<String> animation = new Input<>("animation", "Animation", StringType.INSTANCE)
                .options("damage", "critical", "magic_critical", "main_hand", "off_hand", "wake_up");
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            player.getValue(ctx).tryUse(ctx, p -> {
                switch (animation.getValue(ctx)) {
                    case "damage" -> p.animateHurt(0);
                    case "critical" -> p.crit(p);
                    case "magic_critical" -> p.magicCrit(p);
                    case "main_hand" -> p.swing(InteractionHand.MAIN_HAND);
                    case "off_hand" -> p.swing(InteractionHand.OFF_HAND);
                    case "wake_up" -> p.connection.send(new ClientboundAnimatePacket(p, ClientboundAnimatePacket.WAKE_UP));
                };
            });
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new PlayerAnimationNode();
    }
}
