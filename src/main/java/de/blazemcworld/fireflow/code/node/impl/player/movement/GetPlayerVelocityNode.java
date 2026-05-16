package de.blazemcworld.fireflow.code.node.impl.player.movement;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.VectorType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

public class GetPlayerVelocityNode extends Node {
    public GetPlayerVelocityNode() {
        super("get_player_velocity", "Get Player Velocity", "Gets the player's velocity", Items.ARROW);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Output<Vec3d> velocity = new Output<>("velocity", "Velocity", VectorType.INSTANCE);

        velocity.valueFrom(ctx -> player.getValue(ctx).tryGet(ctx, p -> p.getVelocity(), null));
    }

    @Override
    public Node copy() {
        return new GetPlayerVelocityNode();
    }
}
