package de.blazemcworld.fireflow.code.widget;

import de.blazemcworld.fireflow.code.CodeInteraction;
import net.minecraft.text.TextColor;

import java.util.List;

public class BorderWidget<T extends Widget> extends Widget {

    public final T inner;
    private final RectElement rect;
    public double padding = 1/8f;
    public double margin = 0f;
    private FilledRectElement background = null;

    public BorderWidget(T inner) {
        super(inner.pos());
        this.inner = inner;
        rect = new RectElement(inner.pos());
    }

    @Override
    public WidgetVec size() {
        return inner.size().add((padding + margin) * 2, (padding + margin) * 2);
    }

    @Override
    public List<Widget> getChildren() {
        return List.of(inner);
    }

    @Override
    public boolean interact(CodeInteraction i) {
        if (!inBounds(i.pos())) return false;
        return inner.interact(i);
    }

    @Override
    public void update() {
        WidgetVec current = pos().sub(margin, margin);

        rect.pos = current;
        rect.size = inner.size().add(padding * 2, padding * 2);
        rect.update();

        current = current.add(-padding, -padding);
        inner.pos(current);
        inner.update();

        if (background != null) {
            background.pos = rect.pos;
            background.size = rect.size;
            background.update();
        }
    }

    @Override
    public void remove() {
        rect.remove();
        inner.remove();
        if (background != null) {
            background.remove();
        }
    }

    public void color(TextColor color) {
        rect.color(color);
    }

    public void backgroundColor(int argb) {
        if (argb == 0) {
            if (background != null) {
                background.remove();
                background = null;
            }
            return;
        }
        if (background == null) {
            background = new FilledRectElement(pos(), argb);
            background.pos = pos();
            background.size = size();
        } else {
            background.color = argb;
        }
    }
}
