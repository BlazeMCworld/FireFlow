package de.blazemcworld.fireflow.code.node.impl.position;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.type.PositionType;
import de.blazemcworld.fireflow.code.value.Position;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

public class SetPositionComponentNode extends Node {
    public SetPositionComponentNode() {
        super("set_position_component", "Set Position Component", "Changes a single component of a position", Items.COMPASS);
        
        Input<Position> position = new Input<>("position", "Position", PositionType.INSTANCE);
        Input<String> component = new Input<>("component", "Component", StringType.INSTANCE)
                .options("X", "Y", "Z", "Pitch", "Yaw");
        Input<Double> value = new Input<>("value", "Value", NumberType.INSTANCE);
        Output<Position> output = new Output<>("output", "Output", PositionType.INSTANCE);

        output.valueFrom((ctx) -> {
            Position inputPos = position.getValue(ctx);
            double newValue = value.getValue(ctx);
            return switch (component.getValue(ctx)) {
                case "X" -> new Position(new Vec3d(newValue, inputPos.xyz().y, inputPos.xyz().z), inputPos.pitch(), inputPos.yaw());
                case "Y" -> new Position(new Vec3d(inputPos.xyz().x, newValue, inputPos.xyz().z), inputPos.pitch(), inputPos.yaw());
                case "Z" -> new Position(new Vec3d(inputPos.xyz().x, inputPos.xyz().y, newValue), inputPos.pitch(), inputPos.yaw());
                case "Pitch" -> new Position(inputPos.xyz(), (float) newValue, inputPos.yaw());
                case "Yaw" -> new Position(inputPos.xyz(), inputPos.pitch(), (float) newValue);
                default -> inputPos;
            };
        });
    }

    @Override
    public Node copy() {
        return new SetPositionComponentNode();
    }
}
