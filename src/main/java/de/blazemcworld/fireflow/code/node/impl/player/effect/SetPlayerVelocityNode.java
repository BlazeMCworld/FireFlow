package de.blazemcworld.fireflow.code.node.impl.player.effect;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.VectorType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

public class SetPlayerVelocityNode extends Node {
    public SetPlayerVelocityNode() {
        super("set_player_velocity", "Set Player Velocity", "Sets the velocity (motion) of the player", Items.ARROW);
        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<Vec3d> velocity = new Input<>("velocity", "Velocity", VectorType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);
        signal.onSignal((ctx) -> {
            player.getValue(ctx).tryUse(ctx, p -> {
                p.setVelocity(velocity.getValue(ctx));
                p.velocityModified = true;
                p.velocityDirty = true;
            });
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new SetPlayerVelocityNode();
    }
}
