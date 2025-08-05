package de.blazemcworld.fireflow.code.node.impl.vector;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.VectorType;
import org.bukkit.Material;
import org.bukkit.util.Vector;

public class DotProductNode extends Node {

    public DotProductNode() {
        super("dot_product", "Dot Product", "Gets the Dot Product of two vectors", Material.GUNPOWDER);

        Input<Vector> first = new Input<>("first", "First", VectorType.INSTANCE);
        Input<Vector> second = new Input<>("second", "Second", VectorType.INSTANCE);
        Output<Double> result = new Output<>("result", "Result", NumberType.INSTANCE);

    	result.valueFrom(ctx -> first.getValue(ctx).dot(second.getValue(ctx)));
    }

    @Override
    public Node copy() {
        return new DotProductNode();
    }
}
