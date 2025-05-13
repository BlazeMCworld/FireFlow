package de.blazemcworld.fireflow.code.widget;


import de.blazemcworld.fireflow.code.CodeInteraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VerticalContainerWidget extends Widget {

    public Align align = Align.LEFT;
    public final List<Widget> widgets = new ArrayList<>();

    public VerticalContainerWidget(WidgetVec pos) {
        super(pos);
    }

    public VerticalContainerWidget(WidgetVec pos, Widget... widgets) {
        super(pos);
        this.widgets.addAll(Arrays.asList(widgets));
    }

    @Override
    public List<Widget> getChildren() {
        return new ArrayList<>(widgets);
    }

    @Override
    public WidgetVec size() {
        WidgetVec size = new WidgetVec(pos().editor(), 0, 0);

        for (Widget widget : widgets) {
            WidgetVec widgetSize = widget.size();
            size = size.add(0, widgetSize.y())
                    .withX(Math.max(size.x(), widgetSize.x()));
        }

        return size;
    }

    @Override
    public void update() {
        WidgetVec current = pos();

        double width = size().x();
        for (Widget widget : widgets) {
            if (align == Align.LEFT) {
                widget.pos(current);
            } else if (align == Align.RIGHT) {
                widget.pos(current.add(widget.size().x() - width, 0));
            } else {
                widget.pos(current.add(Math.round((widget.size().x() / 2 - width / 2) * 8f) / 8f, 0));
            }
            widget.update();
            current = current.add(0, -widget.size().y());
        }
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
    public boolean interact(CodeInteraction i) {
        if (!inBounds(i.pos())) return false;
        for (Widget w : widgets) {
            if (w.interact(i)) return true;
        }
        return false;
    }
}
