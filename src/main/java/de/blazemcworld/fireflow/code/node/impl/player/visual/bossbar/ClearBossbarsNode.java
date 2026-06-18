package de.blazemcworld.fireflow.code.node.impl.player.visual.bossbar;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import de.blazemcworld.fireflow.space.BossBarManager;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceManager;
import net.minecraft.item.Items;

public class ClearBossbarsNode extends Node {

    public ClearBossbarsNode() {
        super("clear_bossbars", "Clear Bossbars", "Removes all bossbars from a player", Items.STRUCTURE_VOID);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);

        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            player.getValue(ctx).tryUse(ctx, p -> {
                Space space = SpaceManager.getSpaceForPlayer(p);
                BossBarManager bossBarManager = space.bossBarManager;
                bossBarManager.clearBossBars(p);
            });
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new ClearBossbarsNode();
    }
}
