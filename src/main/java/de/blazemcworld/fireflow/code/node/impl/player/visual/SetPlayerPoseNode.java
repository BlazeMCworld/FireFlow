package de.blazemcworld.fireflow.code.node.impl.player.visual;

import java.util.Arrays;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.entity.EntityPose;
import net.minecraft.item.Items;

public class SetPlayerPoseNode extends Node {
    public SetPlayerPoseNode() {
        super("set_player_pose", "Set Player Pose", "Updates the player's pose to the specified pose", Items.ARMOR_STAND);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<String> pose = new Input<>("pose", "Pose", StringType.INSTANCE).options(Arrays.stream(EntityPose.values()).map(s -> s.name().toLowerCase()).toArray(String[]::new));
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            player.getValue(ctx).tryUse(ctx, p -> p.setPose(EntityPose.valueOf(pose.getValue(ctx).toUpperCase())));
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new SetPlayerPoseNode();
    }
}
