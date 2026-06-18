package de.blazemcworld.fireflow.code.node.impl.player.visual.bossbar;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.TextType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import de.blazemcworld.fireflow.space.BossBarManager;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceManager;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.List;

public class RemoveBossbarNode extends Node {

    public RemoveBossbarNode() {
        super("remove_bossbar", "Remove Bossbar", "Removes a bossbar from a player", Items.PALE_OAK_SIGN);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<Double> position = new Input<>("position", "Position", NumberType.INSTANCE);

        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            player.getValue(ctx).tryUse(ctx, p -> {
                Space space = SpaceManager.getSpaceForPlayer(p);
                BossBarManager bossBarManager = space.bossBarManager;
                bossBarManager.removeBossBarAtIndex(p,position.getValue(ctx).intValue());
            });
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new RemoveBossbarNode();
    }
}
