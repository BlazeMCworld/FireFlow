package de.blazemcworld.fireflow.code.node.impl.player.info;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;

public class GetPlayerUUIDNode extends Node {
    public GetPlayerUUIDNode() {
        super("get_player_uuid", "Get Player UUID", "Gets the UUID of the player", Items.GOLDEN_APPLE);

        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Output<String> uuid = new Output<>("uuid", "UUID", StringType.INSTANCE);

        uuid.valueFrom(ctx -> player.getValue(ctx).uuid.toString());
    }

    @Override
    public Node copy() {
        return new GetPlayerUUIDNode();
    }
}
