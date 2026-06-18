package de.blazemcworld.fireflow.code.node.impl.vector;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.VectorType;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.core.lookup.Interpolator;

public class DotProductNode extends Node {

    public DotProductNode() {
        super("dot_product", "Dot Product", "Gets the Dot Product of two vectors", Items.GUNPOWDER);

        Input<Vec3d> first = new Input<>("first", "First", VectorType.INSTANCE);
        Input<Vec3d> second = new Input<>("second", "Second", VectorType.INSTANCE);
        Output<Double> result = new Output<>("result", "Result", NumberType.INSTANCE);

    	result.valueFrom(ctx -> first.getValue(ctx).dotProduct(second.getValue(ctx)));
    }

    @Override
    public Node copy() {
        return new DotProductNode();
    }
}
