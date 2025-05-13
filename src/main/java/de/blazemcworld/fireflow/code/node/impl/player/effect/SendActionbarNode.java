package de.blazemcworld.fireflow.code.node.impl.player.effect;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.TextType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

public class SendActionbarNode extends Node {

    public SendActionbarNode() {
        super("send_actionbar", "Send Actionbar", "Sends an actionbar message to the player", Items.BAMBOO);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<Text> message = new Input<>("message", "Message", TextType.INSTANCE);

        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            player.getValue(ctx).tryUse(ctx, p -> p.sendMessage(message.getValue(ctx), true));
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new SendActionbarNode();
    }
}

