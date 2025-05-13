package de.blazemcworld.fireflow.code.widget;

import de.blazemcworld.fireflow.code.CodeInteraction;

import java.util.ArrayList;
import java.util.List;

public class GridWidget extends Widget {

    public int maxColumns;
    public double spacing = 0.125;
    public final List<Widget> widgets = new ArrayList<>();

    public GridWidget(WidgetVec pos, int maxColumns) {
        super(pos);
        this.maxColumns = maxColumns;
    }

    @Override
    public WidgetVec size() {
        WidgetVec tiles = tileSize();
        int columns = Math.min(widgets.size(), maxColumns);
        if (columns == 0) return new WidgetVec(pos().editor(), 0, 0);
        int rows = widgets.size() / columns;
        if (widgets.size() % columns != 0) rows++;

        return new WidgetVec(
                pos().editor(),
                tiles.x() * columns + (spacing * Math.max(0, columns - 1)),
                tiles.y() * rows + (spacing * Math.max(0, rows - 1))
        );
    }

    @Override
    public void update() {
        WidgetVec tiles = tileSize();
        WidgetVec origin = pos().sub(tiles.div(2));
        WidgetVec current = origin;
        int column = 0;
        for (Widget w : widgets) {
            w.pos(current.add(w.size().div(2)));
            w.update();
            current = current.sub(tiles.x() + spacing, 0);
            if (++column >= maxColumns) {
                current = current.sub(0, tiles.y() + spacing).withX(origin.x());
                column = 0;
            }
        }
    }

    private WidgetVec tileSize() {
        WidgetVec size = new WidgetVec(pos().editor(), 0, 0);

        for (Widget w : widgets) {
            size = size.max(w.size());
        }
        return size;
    }

    @Override
    public List<Widget> getChildren() {
        return new ArrayList<>(widgets);
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
