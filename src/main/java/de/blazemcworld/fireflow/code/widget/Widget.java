package de.blazemcworld.fireflow.code.widget;

import de.blazemcworld.fireflow.code.Interaction;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.InstanceContainer;

import java.util.List;

public interface Widget {

    void setPos(Vec pos);
    Vec getPos();
    Vec getSize();
    void update(InstanceContainer inst);
    void remove();
    boolean interact(Interaction i);
    List<Widget> getChildren();

    default boolean inBounds(Vec pos) {
        Vec transformed = getPos().sub(pos);
        Vec size = getSize();
        return transformed.x() >= 0 && transformed.y() >= 0 && transformed.x() < size.x() && transformed.y() < size.y();
    }

    default Widget getWidget(Vec pos) {
        if (!inBounds(pos)) return null;
        return this;
    }
}
