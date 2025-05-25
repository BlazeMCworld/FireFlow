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
        List<Double> xChanges = new ArrayList<>();
        List<Double> yChanges = new ArrayList<>();
        List<Pair<WidgetVec, WidgetVec>> verticalWires = new ArrayList<>();
        List<Pair<WidgetVec, WidgetVec>> horizontalWires = new ArrayList<>();
        List<Pair<WidgetVec, WidgetVec>> obstacles = new ArrayList<>(); // <min, max>

        for (Widget w : editor.rootWidgets) {
            if (!(w instanceof NodeWidget n)) continue;
            WidgetVec size = w.size().gridAligned();
            WidgetVec pos = w.pos().gridAligned();
            obstacles.add(Pair.of(pos.sub(size), pos));

            xChanges.add(pos.x() + 0.25);
            xChanges.add(pos.x() - size.x() - 0.25);
            yChanges.add(pos.y() + 0.25);
            yChanges.add(pos.y() - size.y() - 0.25);

            for (NodeIOWidget io : n.getIOWidgets()) {
                for (WireWidget c : io.connections) {
                    for (WireWidget part : c.getFullWire()) {
                        WidgetVec min = part.line.from.min(part.line.to);
                        WidgetVec max = part.line.from.max(part.line.to);
                        if (min.x() == max.x()) verticalWires.add(Pair.of(min, max));
                        else horizontalWires.add(Pair.of(min, max));
                        xChanges.add(min.x() - 0.25);
                        xChanges.add(max.x() + 0.25);
                        yChanges.add(min.y() - 0.25);
                        yChanges.add(max.y() + 0.25);
                    }
                }
            }
        }

        // Check if end is inside an obstacle (fast exit)
        for (Pair<WidgetVec, WidgetVec> obstacle : obstacles) {
            WidgetVec minWall = obstacle.left();
            WidgetVec maxWall = obstacle.right();
            if (end.y() > maxWall.y() || end.y() < minWall.y()) continue;
            if (end.x() < minWall.x() || end.x() > maxWall.x()) continue;
            return getFallback(start, end);
        }

        xChanges.add(end.x());
        yChanges.add(end.y());

        xChanges = new ArrayList<>(new HashSet<>(xChanges));
        yChanges = new ArrayList<>(new HashSet<>(yChanges));
        verticalWires = new ArrayList<>(new HashSet<>(verticalWires));
        horizontalWires = new ArrayList<>(new HashSet<>(horizontalWires));

        xChanges.sort(Double::compareTo);
        yChanges.sort(Double::compareTo);

        HashMap<Point, Point> known = new HashMap<>();
        PriorityQueue<Point> todo = new PriorityQueue<>(Comparator.comparing(p -> p.penalty));
        Point startP = new Point(start.gridAligned(), 0.0, null);
        todo.add(startP);
        known.put(startP, startP);

        end = end.gridAligned();
        int computeLimit = 1024;
        while (!todo.isEmpty()) {
            if (--computeLimit < 0) break;
            Point current = todo.poll();

            if (end.equals(current.pos)) {
                List<WidgetVec> path = new ArrayList<>();
                while (current != null) {
                    path.add(current.pos);
                    current = current.origin;
                }
                Collections.reverse(path);
                return path;
            }

            Pair<Double, Double> minMax = validXRange(current.pos, obstacles);
            for (double xChange : xChanges) {
                if (minMax.left() >= xChange) continue;
                if (minMax.right() <= xChange) break;
                WidgetVec v = new WidgetVec(editor, xChange, current.pos.y());
                double p = current.penalty + 5;

                double xMin = Math.min(xChange, current.pos.x());
                double xMax = Math.max(xChange, current.pos.x());
                for (Pair<WidgetVec, WidgetVec> wire : horizontalWires) {
                    if (wire.left().y() != current.pos.y()) continue;
                    if (wire.left().x() > xMax || wire.right().x() < xMin) continue;
                    p += 5;
                }

                p += current.pos.distanceSquared(v);
                Point next = new Point(v, p, current);
                Point old = known.get(next);
                if (old != null && old.penalty <= p) continue;
                known.put(next, next);
                todo.add(next);
            }

            minMax = validYRange(current.pos, obstacles);
            for (double yChange : yChanges) {
                if (minMax.left() >= yChange) continue;
                if (minMax.right() <= yChange) break;
                WidgetVec v = new WidgetVec(editor, current.pos.x(), yChange);
                double p = current.penalty + 5;

                double yMin = Math.min(yChange, current.pos.y());
                double yMax = Math.max(yChange, current.pos.y());
                for (Pair<WidgetVec, WidgetVec> wire : verticalWires) {
                    if (wire.left().x() != current.pos.x()) continue;
                    if (wire.left().y() > yMax || wire.right().y() < yMin) continue;
                    p += 5;
                }

                p += current.pos.distanceSquared(v);
                Point next = new Point(v, p, current);
                Point old = known.get(next);
                if (old != null && old.penalty <= p) continue;
                known.put(next, next);
                todo.add(next);
            }
        }

        return getFallback(start, end);
    }

    private List<WidgetVec> getFallback(WidgetVec start, WidgetVec end) {
        List<WidgetVec> path = new ArrayList<>();
        path.add(start);
        path.add(start.withX((start.x() + end.x()) / 2).gridAligned());
        path.add(end.withX((start.x() + end.x()) / 2).gridAligned());
        path.add(end);
        return path;
    }

    private Pair<Double, Double> validXRange(WidgetVec current, List<Pair<WidgetVec, WidgetVec>> obstacles) {
        double min = Double.NEGATIVE_INFINITY;
        double max = Double.POSITIVE_INFINITY;
        for (Pair<WidgetVec, WidgetVec> obstacle : obstacles) {
            WidgetVec minWall = obstacle.left();
            WidgetVec maxWall = obstacle.right();
            if (current.y() > maxWall.y() || current.y() < minWall.y()) continue;
            if (current.x() < minWall.x()) max = Math.min(max, minWall.x());
            if (current.x() > maxWall.x()) min = Math.max(min, maxWall.x());
        }
        return Pair.of(min, max);
    }

    private Pair<Double, Double> validYRange(WidgetVec current, List<Pair<WidgetVec, WidgetVec>> obstacles) {
        double min = Double.NEGATIVE_INFINITY;
        double max = Double.POSITIVE_INFINITY;
        for (Pair<WidgetVec, WidgetVec> obstacle : obstacles) {
            WidgetVec minWall = obstacle.left();
            WidgetVec maxWall = obstacle.right();
            if (current.x() > maxWall.x() || current.x() < minWall.x()) continue;
            if (current.y() < minWall.y()) max = Math.min(max, minWall.y());
            if (current.y() > maxWall.y()) min = Math.max(min, maxWall.y());
        }
        return Pair.of(min, max);
    }

    private static class Point {
        WidgetVec pos;
        double penalty;
        Point origin;

        public Point(WidgetVec pos, double penalty, Point origin) {
            this.pos = pos;
            this.penalty = penalty;
            this.origin = origin;
        }

        @Override
        public int hashCode() {
            return pos.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Point other && other.pos.equals(pos);
        }
    }
}
