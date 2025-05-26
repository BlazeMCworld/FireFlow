package de.blazemcworld.fireflow.code.node.impl.player.movement;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.PositionType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import de.blazemcworld.fireflow.code.value.Position;
import net.minecraft.item.Items;

import java.util.Set;

public class TeleportPlayerNode extends Node {

    public TeleportPlayerNode() {
        super("teleport_player", "Teleport Player", "Teleports the player to a position", Items.ENDER_PEARL);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<Position> position = new Input<>("position", "Position", PositionType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            player.getValue(ctx).tryUse(ctx, p -> {
                Position pos = position.getValue(ctx);
                p.teleport(ctx.evaluator.world, pos.xyz().x, pos.xyz().y, pos.xyz().z, Set.of(), pos.yaw(), pos.pitch(), true);
            });
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new TeleportPlayerNode();
    }

}
