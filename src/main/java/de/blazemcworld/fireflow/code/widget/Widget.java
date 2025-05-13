package de.blazemcworld.fireflow.code.widget;


import de.blazemcworld.fireflow.code.CodeInteraction;

import java.util.List;

public abstract class Widget {

    private WidgetVec pos;

    public Widget(WidgetVec pos) {
        this.pos = pos;
    }

    public void pos(WidgetVec pos) {
        this.pos = pos;
    }

    public WidgetVec pos() {
        return pos;
    }

    public abstract void update();

    public abstract void remove();

    public abstract WidgetVec size();

    public boolean inBounds(WidgetVec pos) {
        WidgetVec transformed = pos().sub(pos);
        WidgetVec size = size();
        return transformed.x() >= 0 && transformed.y() >= 0 && transformed.x() < size.x() && transformed.y() < size.y();
    }

    public abstract List<Widget> getChildren();

    public abstract boolean interact(CodeInteraction i);
}
