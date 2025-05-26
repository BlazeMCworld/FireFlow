package de.blazemcworld.fireflow.code.node.impl.player.movement;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.PositionType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import de.blazemcworld.fireflow.code.value.Position;
import net.minecraft.item.Items;

public class PlayerPositionNode extends Node {

    public PlayerPositionNode() {
        super("player_position", "Player Position", "Gets the current coordinates of the player.", Items.RECOVERY_COMPASS);

        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Output<Position> position = new Output<>("position", "Position", PositionType.INSTANCE);

        position.valueFrom((ctx) -> player.getValue(ctx)
                .tryGet(ctx, p -> new Position(p.getPos(), p.getPitch(), p.getYaw()), PositionType.INSTANCE.defaultValue()));
    }

    @Override
    public Node copy() {
        return new PlayerPositionNode();
    }

}
