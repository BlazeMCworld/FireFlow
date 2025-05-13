package de.blazemcworld.fireflow.code.node.impl.position;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.PositionType;
import de.blazemcworld.fireflow.code.value.Position;
import net.minecraft.item.Items;

public class PositionDistanceNode extends Node {

    public PositionDistanceNode() {
        super("position_distance", "Position Distance", "Returns the distance between two positions", Items.DIAMOND);

        Input<Position> aPos = new Input<>("a", "A", PositionType.INSTANCE);
        Input<Position> bPos = new Input<>("b", "B", PositionType.INSTANCE);
        Output<Double> distance = new Output<>("distance", "Distance", NumberType.INSTANCE);

        distance.valueFrom(ctx -> aPos.getValue(ctx).xyz().distanceTo(bPos.getValue(ctx).xyz()));
    }

    @Override
    public Node copy() {
        return new PositionDistanceNode();
    }

}
