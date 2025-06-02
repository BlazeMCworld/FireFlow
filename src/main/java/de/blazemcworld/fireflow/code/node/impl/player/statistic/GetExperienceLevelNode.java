package de.blazemcworld.fireflow.code.node.impl.player.statistic;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;

public class GetExperienceLevelNode extends Node {
    public GetExperienceLevelNode() {
        super("get_experience_level", "Get Experience Level", "Gets the experience level of the player", Items.LAPIS_LAZULI);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Output<Double> level = new Output<>("level", "Level", NumberType.INSTANCE);

        level.valueFrom(ctx -> player.getValue(ctx).tryGet(ctx, p -> (double) p.experienceLevel, 0.0));
    }

    @Override
    public Node copy() {
        return new GetExperienceLevelNode();
    }
}
