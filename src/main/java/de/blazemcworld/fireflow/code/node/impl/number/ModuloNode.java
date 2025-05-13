package de.blazemcworld.fireflow.code.node.impl.number;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import net.minecraft.item.Items;

public class ModuloNode extends Node {

    public ModuloNode() {
        super("modulo_number", "Modulo", "Calculates the remainder of division between two numbers", Items.BONE_MEAL);

        Input<Double> left = new Input<>("left", "Left", NumberType.INSTANCE);
        Input<Double> right = new Input<>("right", "Right", NumberType.INSTANCE);
        Output<Double> result = new Output<>("result", "Result", NumberType.INSTANCE);

        result.valueFrom((ctx) -> {
            double l = left.getValue(ctx);
            double r = right.getValue(ctx);
            return ((l % r) + r) % r;
        });
    }

    @Override
    public Node copy() {
        return new ModuloNode();
    }
}

