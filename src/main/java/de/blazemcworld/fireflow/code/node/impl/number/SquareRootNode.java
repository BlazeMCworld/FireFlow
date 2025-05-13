package de.blazemcworld.fireflow.code.node.impl.number;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import net.minecraft.item.Items;

public class SquareRootNode extends Node {

    public SquareRootNode() {
        super("square_root", "Square Root", "Calculates the square root of a number", Items.BEETROOT);

        Input<Double> number = new Input<>("number", "Number", NumberType.INSTANCE);
        Output<Double> result = new Output<>("result", "Result", NumberType.INSTANCE);

        result.valueFrom((ctx) -> Math.sqrt(number.getValue(ctx)));
    }

    @Override
    public Node copy() {
        return new SquareRootNode();
    }

}
