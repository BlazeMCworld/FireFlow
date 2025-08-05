package de.blazemcworld.fireflow.code.node.impl.player.meta;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class GetPlayerPingNode extends Node {
    public GetPlayerPingNode() {
        super("get_player_ping", "Get Player Ping", "Gets the ping of a player.", Material.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE);

        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Output<Double> ping = new Output<>("ping", "Ping", NumberType.INSTANCE);

        ping.valueFrom(ctx -> (double) player.getValue(ctx).tryGet(ctx, Player::getPing, 0));
    }

    @Override
    public Node copy() {
        return new GetPlayerPingNode();
    }
}