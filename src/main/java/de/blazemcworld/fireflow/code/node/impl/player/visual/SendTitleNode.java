package de.blazemcworld.fireflow.code.node.impl.player.visual;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.TextType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.text.Text;

public class SendTitleNode extends Node {
    public SendTitleNode() {
        super("send_title", "Send Title", "Sends a title message to the player", Items.DARK_OAK_SIGN);
        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<Text> title = new Input<>("title", "Title", TextType.INSTANCE);
        Input<Text> subtitle = new Input<>("subtitle", "Subtitle", TextType.INSTANCE);
        Input<Double> fade_in = new Input<>("fade_in", "Fade In", NumberType.INSTANCE);
        Input<Double> stay_number = new Input<>("stay", "Stay", NumberType.INSTANCE);
        Input<Double> fade_out = new Input<>("fade_out", "Fade Out", NumberType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);
        signal.onSignal((ctx) -> {
            player.getValue(ctx).tryUse(ctx, p -> {
                p.networkHandler.sendPacket(new TitleS2CPacket(title.getValue(ctx)));
                p.networkHandler.sendPacket(new SubtitleS2CPacket(subtitle.getValue(ctx)));
                p.networkHandler.sendPacket(new TitleFadeS2CPacket(
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
