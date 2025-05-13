package de.blazemcworld.fireflow.code.widget;

import net.minecraft.text.TextColor;

public class RectElement {

    public WidgetVec pos;
    public WidgetVec size;
    private final LineElement top;
    private final LineElement bottom;
    private final LineElement left;
    private final LineElement right;

    public RectElement(WidgetVec pos) {
        this.pos = pos;
        top = new LineElement(pos);
        bottom = new LineElement(pos);
        left = new LineElement(pos);
        right = new LineElement(pos);
    }

    public void update() {
        top.from = pos;
        top.to = pos.add(-size.x(), 0);

        bottom.from = pos.add(0, -size.y());
        bottom.to = pos.add(-size.x(), -size.y());

        left.from = pos;
        left.to = pos.add(0, -size.y());

        right.from = pos.add(-size.x(), 0);
        right.to = pos.add(-size.x(), -size.y());

        top.update();
        bottom.update();
        left.update();
        right.update();
    }

    public void remove() {
        top.remove();
        bottom.remove();
        left.remove();
        right.remove();
    }

    public void color(TextColor color) {
        top.color(color);
        bottom.color(color);
        left.color(color);
        right.color(color);
    }
}
