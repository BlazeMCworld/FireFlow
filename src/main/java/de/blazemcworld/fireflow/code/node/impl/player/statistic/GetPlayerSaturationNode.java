package de.blazemcworld.fireflow.code.node.impl.player.statistic;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;

public class GetPlayerSaturationNode extends Node {
    public GetPlayerSaturationNode() {
        super("get_player_saturation", "Get Player Saturation", "Gets the saturation of the player", Items.GOLDEN_CARROT);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Output<Double> saturation = new Output<>("saturation", "Saturation", NumberType.INSTANCE);

        saturation.valueFrom(ctx -> player.getValue(ctx).tryGet(ctx, p -> (double) p.getHungerManager().getSaturationLevel(), 0.0));
    }

    @Override
    public Node copy() {
        return new GetPlayerSaturationNode();
    }
}
