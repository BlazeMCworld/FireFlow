package de.blazemcworld.fireflow.code.widget;

import de.blazemcworld.fireflow.code.CodeInteraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HorizontalContainerWidget extends Widget {

    public final List<Widget> widgets = new ArrayList<>();

    public HorizontalContainerWidget(WidgetVec pos) {
        super(pos);
    }

    public HorizontalContainerWidget(WidgetVec pos, Widget... widgets) {
        super(pos);
        this.widgets.addAll(Arrays.asList(widgets));
    }

    @Override
    public WidgetVec size() {
        WidgetVec size = new WidgetVec(pos().editor(), 0, 0);

        for (Widget widget : widgets) {
            WidgetVec widgetSize = widget.size();
            size = size.add(widgetSize.x(), 0)
                    .withY(Math.max(size.y(), widgetSize.y()));
        }

        return size;
    }

    @Override
    public List<Widget> getChildren() {
        return new ArrayList<>(widgets);
    }

    @Override
    public void update() {
        WidgetVec current = pos();

        for (Widget widget : widgets) {
            widget.pos(current);
            widget.update();
            current = current.sub(widget.size().x(), 0);
        }
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
