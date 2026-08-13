package de.blazemcworld.fireflow.code.node.impl.vector;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.type.VectorType;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class SetVectorComponentNode extends Node {
    public SetVectorComponentNode() {
        super("set_vector_component", "Set Vector Component", "Changes a single axis of a vector", Items.PRISMARINE_SHARD);
        Input<Vec3> vector = new Input<>("vector", "Vector", VectorType.INSTANCE);
        Input<String> axis = new Input<>("axis", "Axis", StringType.INSTANCE).options("X", "Y", "Z");
        Input<Double> value = new Input<>("value", "Value", NumberType.INSTANCE);
        Output<Vec3> output = new Output<>("output", "Output", VectorType.INSTANCE);

        output.valueFrom((ctx) -> {
            Vec3 outputVec = vector.getValue(ctx);
            double outputValue = value.getValue(ctx);
            return switch (axis.getValue(ctx)) {
                case "X" -> outputVec.with(Axis.X, outputValue);
                case "Y" -> outputVec.with(Axis.Y, outputValue);
                case "Z" -> outputVec.with(Axis.Z, outputValue);
                default -> outputVec;
            };
        });
    }


    @Override
    public Node copy() {
        return new SetVectorComponentNode();
    }
}