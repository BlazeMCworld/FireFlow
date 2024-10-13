package de.blazemcworld.fireflow.code.widget;

import de.blazemcworld.fireflow.code.Interaction;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.InstanceContainer;

import java.util.ArrayList;
import java.util.List;

public class HorizontalContainerWidget implements Widget {

    private Vec pos = Vec.ZERO;
    public final List<Widget> widgets = new ArrayList<>();

    @Override
    public void setPos(Vec pos) {
        this.pos = pos;
    }

    @Override
    public Vec getPos() {
        return pos;
    }

    @Override
    public Vec getSize() {
        Vec size = Vec.ZERO;

        for (Widget widget : widgets) {
            Vec widgetSize = widget.getSize();
            size = size.add(widgetSize.x(), 0, 0)
                    .withY(Math.max(size.y(), widgetSize.y()));
        }

        return size;
    }

    @Override
    public void update(InstanceContainer inst) {
        Vec current = pos;

        for (Widget widget : widgets) {
            widget.setPos(current);
            widget.update(inst);
            current = current.add(-widget.getSize().x(), 0, 0);
        }
    }

    @Override
    public boolean interact(Interaction i) {
        if (!inBounds(i.pos())) return false;
        for (Widget w : widgets) {
            if (w.interact(i)) return true;
        }
        return false;
    }

    @Override
    public void remove() {
        for (Widget w : widgets) {
            w.remove();
        }
    }

    @Override
    public Widget getWidget(Vec pos) {
        if (!inBounds(pos)) return null;
        for (Widget w : widgets) {
            if (w.inBounds(pos)) return w.getWidget(pos);
        }
        return null;
    }

    @Override
    public List<Widget> getChildren() {
        return widgets;
    }
}
