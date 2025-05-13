package de.blazemcworld.fireflow.code.widget;

import de.blazemcworld.fireflow.code.CodeInteraction;

import java.util.List;

public class SpacingWidget extends Widget {

    public WidgetVec size;

    public SpacingWidget(WidgetVec pos, WidgetVec size) {
        super(pos);
        this.size = size;
    }

    @Override
    public List<Widget> getChildren() {
        return List.of();
    }

    @Override
    public void update() {
    }

    @Override
    public void remove() {
    }

    @Override
    public WidgetVec size() {
        return size;
    }

    @Override
    public boolean interact(CodeInteraction i) {
        return false;
    }
}
