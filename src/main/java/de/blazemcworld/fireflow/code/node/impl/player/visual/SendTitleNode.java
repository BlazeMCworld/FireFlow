package de.blazemcworld.fireflow.code.node.impl.player.visual;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.TextType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.world.item.Items;

public class SendTitleNode extends Node {
    public SendTitleNode() {
        super("send_title", "Send Title", "Sends a title message to the player", Items.DARK_OAK_SIGN);
        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<Component> title = new Input<>("title", "Title", TextType.INSTANCE);
        Input<Component> subtitle = new Input<>("subtitle", "Subtitle", TextType.INSTANCE);
        Input<Double> fade_in = new Input<>("fade_in", "Fade In", NumberType.INSTANCE);
        Input<Double> stay_number = new Input<>("stay", "Stay", NumberType.INSTANCE);
        Input<Double> fade_out = new Input<>("fade_out", "Fade Out", NumberType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);
        signal.onSignal((ctx) -> {
            player.getValue(ctx).tryUse(ctx, p -> {
                p.connection.send(new ClientboundSetTitleTextPacket(title.getValue(ctx)));
                p.connection.send(new ClientboundSetSubtitleTextPacket(subtitle.getValue(ctx)));
                p.connection.send(new ClientboundSetTitlesAnimationPacket(
                        fade_in.getValue(ctx).intValue(),
                        stay_number.getValue(ctx).intValue(),
                        fade_out.getValue(ctx).intValue()
                ));
            });
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new SendTitleNode();
    }
}
