package de.blazemcworld.fireflow.code.node.impl.player.visual;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.TextType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

public class SetTablistNode extends Node {

    public SetTablistNode() {
        super("set_tablist", "Set Tablist", "Sets a tablist for a player", Material.OBSERVER);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<Component> header = new Input<>("header", "Header", TextType.INSTANCE);
        Input<Component> footer = new Input<>("footer", "Footer", TextType.INSTANCE);

        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            player.getValue(ctx).tryUse(ctx, p -> {
                    p.sendPlayerListHeader(header.getValue(ctx));
                    p.sendPlayerListFooter(footer.getValue(ctx));
            });
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new SetTablistNode();
    }
}
