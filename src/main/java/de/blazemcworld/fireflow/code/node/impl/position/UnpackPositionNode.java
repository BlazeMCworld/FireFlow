package de.blazemcworld.fireflow.code.node.impl.position;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.PositionType;
import de.blazemcworld.fireflow.code.value.Position;
import net.minecraft.item.Items;

public class UnpackPositionNode extends Node {

    public UnpackPositionNode() {
        super("unpack_position", "Unpack Position", "Unpacks a position into its components", Items.GOLD_INGOT);

        Input<Position> position = new Input<>("position", "Position", PositionType.INSTANCE);
        Output<Double> x = new Output<>("x", "X", NumberType.INSTANCE);
        Output<Double> y = new Output<>("y", "Y", NumberType.INSTANCE);
        Output<Double> z = new Output<>("z", "Z", NumberType.INSTANCE);
        Output<Double> pitch = new Output<>("pitch", "Pitch", NumberType.INSTANCE);
        Output<Double> yaw = new Output<>("yaw", "Yaw", NumberType.INSTANCE);

        x.valueFrom(ctx -> position.getValue(ctx).xyz().x);
        y.valueFrom(ctx -> position.getValue(ctx).xyz().y);
        z.valueFrom(ctx -> position.getValue(ctx).xyz().z);
        pitch.valueFrom(ctx -> (double) position.getValue(ctx).pitch());
        yaw.valueFrom(ctx -> (double) position.getValue(ctx).yaw());
    }

    @Override
    public Node copy() {
        return new UnpackPositionNode();
    }
}
