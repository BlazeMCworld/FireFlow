package de.blazemcworld.fireflow.code.widget;

import de.blazemcworld.fireflow.code.Interaction;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.InstanceContainer;

import java.util.ArrayList;
import java.util.List;

public class VerticalContainerWidget implements Widget {

    private Vec pos = Vec.ZERO;
    public Align align = Align.LEFT;
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
            size = size.add(0, widgetSize.y(), 0)
                    .withX(Math.max(size.x(), widgetSize.x()));
        }

        return size;
    }

    @Override
    public void update(InstanceContainer inst) {
        Vec current = pos;

        double width = getSize().x();
        for (Widget widget : widgets) {
            if (align == Align.LEFT) {
                widget.setPos(current);
            } else if (align == Align.RIGHT) {
                widget.setPos(current.add(widget.getSize().x() - width, 0, 0));
            } else {
                widget.setPos(current.add(Math.round((widget.getSize().x() / 2 - width / 2) * 8f) / 8f, 0, 0));
            }
            widget.update(inst);
            current = current.add(0, -widget.getSize().y(), 0);
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

    public enum Align {
        LEFT, RIGHT, CENTER
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
