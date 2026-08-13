package de.blazemcworld.fireflow.code.node.impl.vector;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.VectorType;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class AddVectorsNode extends Node {

    public AddVectorsNode() {
        super("add_vectors", "Add Vectors", "Adds together each axis independently.", Items.ANVIL);

        Input<Vec3> first = new Input<>("first", "First", VectorType.INSTANCE);
        Input<Vec3> second = new Input<>("second", "Second", VectorType.INSTANCE);
        Output<Vec3> result = new Output<>("result", "Result", VectorType.INSTANCE);

    	result.valueFrom(ctx -> first.getValue(ctx).add(second.getValue(ctx)));
    }

    @Override
    public Node copy() {
        return new AddVectorsNode();
    }
}
