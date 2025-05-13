package de.blazemcworld.fireflow.code;

import de.blazemcworld.fireflow.code.widget.*;
import it.unimi.dsi.fastutil.Pair;

import java.util.*;

public class Pathfinder {

    private final CodeEditor editor;

    public Pathfinder(CodeEditor editor) {
        this.editor = editor;
    }

    public List<WidgetVec> findPath(WidgetVec start, WidgetVec end) {
        HashMap<Widget, WidgetVec> sizes = new HashMap<>();
        List<Double> xChanges = new ArrayList<>();
        List<Double> yChanges = new ArrayList<>();
        List<Pair<WidgetVec, WidgetVec>> wires = new ArrayList<>();

        for (Widget w : editor.rootWidgets) {
            if (!(w instanceof NodeWidget n)) continue;
            sizes.put(w, w.size());

            WidgetVec size = sizes.computeIfAbsent(w, Widget::size);
            WidgetVec pos = w.pos();
            xChanges.add(pos.x() + 0.25);
            xChanges.add(pos.x() - size.x() - 0.25);
            yChanges.add(pos.y() + 0.25);
            yChanges.add(pos.y() - size.y() - 0.25);

            for (NodeIOWidget io : n.getIOWidgets()) {
                for (WireWidget c : io.connections) {
                    for (WireWidget part : c.getFullWire()) {
                        WidgetVec min = part.line.from.min(part.line.to);
                        WidgetVec max = part.line.from.max(part.line.to);
                        wires.add(Pair.of(min, max));
                        xChanges.add(min.x() - 0.25);
                        xChanges.add(max.x() + 0.25);
                        yChanges.add(min.y() - 0.25);
                        yChanges.add(max.y() + 0.25);
                    }
                }
            }
        }

        xChanges.add(end.x());
        yChanges.add(end.y());

        xChanges = new ArrayList<>(new HashSet<>(xChanges));
        yChanges = new ArrayList<>(new HashSet<>(yChanges));
        wires = new ArrayList<>(new HashSet<>(wires));

        xChanges.sort(Double::compareTo);
        yChanges.sort(Double::compareTo);

        HashMap<WidgetVec, WidgetVec> origins = new HashMap<>();
        HashMap<WidgetVec, Double> penalty = new HashMap<>();
        HashMap<WidgetVec, Double> order = new HashMap<>();
        PriorityQueue<WidgetVec> todo = new PriorityQueue<>(Comparator.comparing(order::get));
        todo.add(start);
        origins.put(start, null);
        penalty.put(start, 0.0);
        order.put(start, 0.0);

        int computeLimit = 256;
        while (!todo.isEmpty()) {
            if (--computeLimit < 0) break;
            WidgetVec current = todo.poll();

            if (current.equals(end)) {
                List<WidgetVec> path = new ArrayList<>();
                while (current != null) {
                    path.add(current);
                    current = origins.get(current);
                }
                Collections.reverse(path);
                return path;
            }

            Pair<Double, Double> minMax = validXRange(current, sizes);
            for (double xChange : xChanges) {
                if (minMax.left() >= xChange) continue;
                if (minMax.right() <= xChange) break;
                WidgetVec v = new WidgetVec(editor, xChange, current.y());
                double p = penalty.get(current) + 25;
                if (current == start) p = 0;
                if (v.equals(end)) p -= 25;

                double xMin = Math.min(xChange, current.x());
                double xMax = Math.max(xChange, current.x());
                for (Pair<WidgetVec, WidgetVec> wire : wires) {
                    if (wire.left().y() != wire.right().y()) continue;
                    if (wire.left().y() != current.y()) continue;
                    if (wire.left().x() > xMax || wire.right().x() < xMin) continue;
                    p += 25;
                }

                p += current.distance(v);
                if (origins.containsKey(v) && penalty.get(v) <= p) continue;
                origins.put(v, current);
                penalty.put(v, p);
                order.put(v, v.distance(end) + p);
                todo.add(v);
            }

            minMax = validYRange(current, sizes);
            for (double yChange : yChanges) {
                if (minMax.left() >= yChange) continue;
                if (minMax.right() <= yChange) break;
                WidgetVec v = new WidgetVec(editor, current.x(), yChange);
                double p = penalty.get(current) + 25;

                double yMin = Math.min(yChange, current.y());
                double yMax = Math.max(yChange, current.y());
                for (Pair<WidgetVec, WidgetVec> wire : wires) {
                    if (wire.left().x() != wire.right().x()) continue;
                    if (wire.left().x() != current.x()) continue;
                    if (wire.left().y() > yMax || wire.right().y() < yMin) continue;
                    p += 25;
                }

                if (origins.containsKey(v) && penalty.get(v) <= p) continue;
                origins.put(v, current);
                penalty.put(v, p);
                order.put(v, v.distanceSquared(end) + p);
                todo.add(v);
            }
        }

        List<WidgetVec> path = new ArrayList<>();
        path.add(start);
        path.add(start.withX(Math.round((start.x() + end.x()) * 4) / 8.0));
        path.add(end.withX(Math.round((start.x() + end.x()) * 4) / 8.0));
        path.add(end);
        return path;
    }

    private Pair<Double, Double> validXRange(WidgetVec current, HashMap<Widget, WidgetVec> sizes) {
        double min = Double.NEGATIVE_INFINITY;
        double max = Double.POSITIVE_INFINITY;
        for (Widget w : editor.rootWidgets) {
            if (!(w instanceof NodeWidget)) continue;

            WidgetVec maxWall = w.pos();
            WidgetVec minWall = maxWall.sub(sizes.get(w));
            if (current.y() > maxWall.y() || current.y() < minWall.y()) continue;
            if (current.x() < minWall.x()) max = Math.min(max, minWall.x());
            if (current.x() > maxWall.x()) min = Math.max(min, maxWall.x());
        }
        return Pair.of(min, max);
    }

    private Pair<Double, Double> validYRange(WidgetVec current, HashMap<Widget, WidgetVec> sizes) {
        double min = Double.NEGATIVE_INFINITY;
        double max = Double.POSITIVE_INFINITY;
        for (Widget w : editor.rootWidgets) {
            if (!(w instanceof NodeWidget)) continue;

            WidgetVec maxWall = w.pos();
            WidgetVec minWall = maxWall.sub(sizes.get(w));
            if (current.x() > maxWall.x() || current.x() < minWall.x()) continue;
            if (current.y() < minWall.y()) max = Math.min(max, minWall.y());
            if (current.y() > maxWall.y()) min = Math.max(min, maxWall.y());
        }
        return Pair.of(min, max);
    }
}
