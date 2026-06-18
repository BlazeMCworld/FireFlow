package de.blazemcworld.fireflow.code.node.impl.player.meta;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;

public class GetPlayerPingNode extends Node {
    public GetPlayerPingNode() {
        super("get_player_ping", "Get Player Ping", "Gets the ping of a player.", Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE);

        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Output<Double> ping = new Output<>("ping", "Ping", NumberType.INSTANCE);

        ping.valueFrom(ctx -> (double) player.getValue(ctx).tryGet(ctx, p -> p.networkHandler.getLatency(), 0));
    }

    @Override
    public Node copy() {
        return new GetPlayerPingNode();
    }
}