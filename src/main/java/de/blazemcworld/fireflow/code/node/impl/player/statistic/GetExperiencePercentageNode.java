package de.blazemcworld.fireflow.code.node.impl.player.statistic;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;

public class GetExperiencePercentageNode extends Node {
    public GetExperiencePercentageNode() {
        super("get_experience_percentage", "Get Experience Percentage", "Gets the experience percentage of the player", Items.EXPERIENCE_BOTTLE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Output<Double> percentage = new Output<>("percentage", "Percentage", NumberType.INSTANCE);

        percentage.valueFrom(ctx -> player.getValue(ctx).tryGet(ctx, p -> (double) p.experienceProgress, 0.0));
    }

    @Override
    public Node copy() {
        return new GetExperiencePercentageNode();
    }
}

