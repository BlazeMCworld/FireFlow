package de.blazemcworld.fireflow.code.node.impl.player.info;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;

public class GetPlayerNameNode extends Node {
    public GetPlayerNameNode() {
        super("get_player_name", "Get Player Name", "Gets the name of the player", Items.NAME_TAG);

        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Output<String> name = new Output<>("name", "Name", StringType.INSTANCE);

        name.valueFrom(ctx -> player.getValue(ctx).tryGet(ctx, p -> p.getGameProfile().getName(), ""));
    }

    @Override
    public Node copy() {
        return new GetPlayerNameNode();
    }
}