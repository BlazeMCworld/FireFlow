package de.blazemcworld.fireflow.editor.widget;

import de.blazemcworld.fireflow.editor.Bounds;
import de.blazemcworld.fireflow.editor.Widget;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.InstanceContainer;

public class RectWidget implements Widget  {

    private final LineWidget a;
    private final LineWidget b;
    private final LineWidget c;
    private final LineWidget d;

    public RectWidget(InstanceContainer inst, Bounds area) {
        a = new LineWidget(inst);
        b = new LineWidget(inst);
        c = new LineWidget(inst);
        d = new LineWidget(inst);
        update(area);
    }

    public void update(Bounds area) {
        a.from = area.min;
        a.to = Vec.fromPoint(area.min).withX(area.max.x());
        d.from = a.to;
        d.to = area.max;
        b.from = d.to;
        b.to = Vec.fromPoint(area.min).withY(area.max.y());
        c.from = b.to;
        c.to = a.from;
        a.update();
        b.update();
        c.update();
        d.update();
    }

    @Override
    public void remove() {
        a.remove();
        b.remove();
        c.remove();
        d.remove();
    }
}