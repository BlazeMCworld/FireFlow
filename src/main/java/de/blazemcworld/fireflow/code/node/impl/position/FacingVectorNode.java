package de.blazemcworld.fireflow.code.node.impl.position;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.PositionType;
import de.blazemcworld.fireflow.code.type.VectorType;
import de.blazemcworld.fireflow.code.value.Position;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class FacingVectorNode extends Node {

    public FacingVectorNode() {
        super("facing_vector", "Facing Vector", "Returns the direction of a position", Items.ENDER_EYE);

        Input<Position> position = new Input<>("position", "Position", PositionType.INSTANCE);
        Output<Vec3> vector = new Output<>("vector", "Vector", VectorType.INSTANCE);

        vector.valueFrom(ctx -> {
            Position pos = position.getValue(ctx);
            double xz = Math.cos(Math.toRadians(pos.pitch()));
            return new Vec3(
                Math.sin(Math.toRadians(pos.yaw())) * -xz,
                -Math.sin(Math.toRadians(pos.pitch())),
                Math.cos(Math.toRadians(pos.yaw())) * xz
            );
        });
    }

    @Override
    public Node copy() {
        return new FacingVectorNode();
    }
}
