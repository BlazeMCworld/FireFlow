package de.blazemcworld.fireflow.code.widget;

import de.blazemcworld.fireflow.code.CodeInteraction;
import de.blazemcworld.fireflow.code.action.DragWireAction;
import de.blazemcworld.fireflow.code.action.WireAction;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.WireType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WireWidget extends Widget {

    public NodeIOWidget previousOutput;
    public List<WireWidget> previousWires = new ArrayList<>();
    public final LineElement line;
    private final List<TextWidget> arrows = new ArrayList<>();
    public List<WireWidget> nextWires = new ArrayList<>();
    public NodeIOWidget nextInput;
    private final WireType<?> type;

    public WireWidget(WidgetVec pos, NodeIOWidget previousOutput, WireType<?> type, WidgetVec cursor) {
        super(pos);
        line = new LineElement(pos);
        if (!previousOutput.isInput()) this.previousOutput = previousOutput;
        else this.nextInput = previousOutput;
        if (previousOutput.isInput()) line.from = previousOutput.pos().sub(1/8f-1/32f, 1/8f);
        else line.from = previousOutput.pos().sub(previousOutput.size().sub(1/8f, 1/8f));
        line.to = cursor;
        line.color(previousOutput.type().color);
        this.type = type;
        doArrows();
    }

    public WireWidget(WidgetVec start, WireType<?> type, WidgetVec end) {
        super(start);
        line = new LineElement(start);
        line.from = start;
        line.to = end;
        line.color(type.color);
        this.type = type;
        doArrows();
    }

    public WireWidget(WireWidget previousWire, WireType<?> type, WidgetVec cursor) {
        super(cursor);
        line = new LineElement(cursor);
        this.previousWires.add(previousWire);
        previousWire.nextWires.add(this);
        line.from = previousWire.line.to;
        line.to = cursor;
        line.color(type.color);
        this.type = type;
        doArrows();
    }

    public WireWidget(WireWidget wire, WireType<?> type, WidgetVec cursor, boolean isNext) {
        super(cursor);
        line = new LineElement(cursor);
        if (!isNext) {
            this.previousWires.add(wire);
            line.from = wire.line.to;
        } else {
            this.nextWires.add(wire);
            line.from = wire.line.from;
        }
        line.to = cursor;
        line.color(type.color);
        this.type = type;
        doArrows();
    }

    public WireWidget(List<WireWidget> previousWires, WireType<?> type, WidgetVec cursor) {
        super(cursor);
        line = new LineElement(cursor);
        this.previousWires.addAll(previousWires);
        line.from = previousWires.getFirst().line.to;
        line.to = cursor;
        line.color(type.color);
        this.type = type;
        doArrows();
    }

    public WireWidget(WireType<?> type, WidgetVec from, WidgetVec to) {
        super(from);
        line = new LineElement(from);
        line.from = from;
        line.to = to;
        line.color(type.color);
        this.type = type;
        doArrows();
    }

    @Override
    public WidgetVec size() {
        return pos().sub(Math.min(line.from.x(), line.to.x()), Math.min(line.from.y(), line.to.y())).add(1/16f, 1/16f);
    }

    @Override
    public void update() {
        line.update();
        doArrows();
        for (TextWidget arrow : arrows) arrow.update();
    }

    private void doArrows() {
        double lineLength = Math.abs((line.from.x() == line.to.x()) ? (line.from.y() - line.to.y()) : (line.from.x() - line.to.x()));
        if (lineLength < 1) {
            arrows.forEach(TextWidget::remove);
            arrows.clear();
            return;
        }
        boolean horizontal = line.from.x() != line.to.x();
        int arrowCount = Math.max((int) Math.ceil(lineLength / 3), 1);
        if (arrowCount > 10) arrowCount = 10;

        Text t = Text.literal(">").setStyle(Style.EMPTY.withColor(type.color).withBold(true));
        for (int i = 0; i < arrowCount; i++) {
            if (arrows.size() <= i) {
                TextWidget arrow = new TextWidget(pos());
                arrow.setText(t);
                arrow.stretch(1.5, 1);
                arrows.add(arrow);
            }
            TextWidget arrow = arrows.get(i);

            WidgetVec pos = new WidgetVec(
                    pos().editor(),
                    horizontal ? (Math.min(line.from.x(), line.to.x()) + (lineLength / (arrowCount + 1)) * (i+1)) : line.from.x() + ((line.from.y() < line.to.y()) ? -0.023 : 0.24775),
                    (!horizontal ? (Math.min(line.from.y(), line.to.y()) + (lineLength / (arrowCount + 1)) * (i+1)) : line.from.y()) + ((line.from.x() > line.to.x()) ? 0.25/1.98 : 0.25*1.585)
            );
            arrow.pos(pos);

            if (!horizontal) {
                if (line.from.y() > line.to.y()) arrow.setRotation(-90);
                else arrow.setRotation(90);
            } else {
                if (line.from.x() > line.to.x()) arrow.setRotation(0);
                else arrow.setRotation(180);
            }
        }

        for (int i = arrowCount; i < arrows.size(); i++) {
            arrows.get(i).remove();
            arrows.remove(i);
            i--;
        }
    }

    @Override
    public void remove() {
        arrows.forEach(TextWidget::remove);
        line.remove();

        if (nextInput != null) {
            nextInput.connections.remove(this);
            nextInput.removed(this);
        }
        if (previousOutput != null) {
            previousOutput.connections.remove(this);
            previousOutput.removed(this);
        }

        for (WireWidget wire : previousWires) {
            wire.nextWires.remove(this);
        }

        for (WireWidget wire : nextWires) {
            wire.previousWires.remove(this);
        }
    }

    public void removeAll() {
        this.remove();
        for (WireWidget wire : previousWires) {
            wire.removeAll();
        }
        pos().editor().rootWidgets.remove(this);
    }

    public void addNextWire(WireWidget nextWire) {
        this.nextWires.add(nextWire);
    }

    public void addNextWires(List<WireWidget> nextWires) {
        this.nextWires.addAll(nextWires);
    }

    public void addPreviousWire(WireWidget previousWire) {
        this.previousWires.add(previousWire);
    }

    public void addPreviousWires(List<WireWidget> previousWires) {
        this.previousWires.addAll(previousWires);
    }

    public void setNextInput(NodeIOWidget nextInput) {
        this.nextInput = nextInput;
    }

    public void setPreviousOutput(NodeIOWidget previousOutput) {
        this.previousOutput = previousOutput;
    }

    private void removeNext() {
        List<WireWidget> nextWiresClone = new ArrayList<>(nextWires);
        this.remove();
        for (WireWidget wire : nextWiresClone) {
            wire.removeNext();
            pos().editor().rootWidgets.remove(wire);
        }
    }

    private void removePrevious() {
        List<WireWidget> previousWiresClone = new ArrayList<>(previousWires);
        this.remove();
        for (WireWidget wire : previousWiresClone) {
            wire.removePrevious();
            pos().editor().rootWidgets.remove(wire);
        }
    }

    private boolean removeWithoutOutputs() {
        if (!this.nextWires.isEmpty() || this.nextInput != null) return false;
        this.remove();
        for (WireWidget wire : previousWires) {
            if (wire.removeWithoutOutputs()) pos().editor().rootWidgets.remove(wire);
        }
        return true;
    }

    private boolean removeWithoutInputs() {
        if (!this.previousWires.isEmpty() || this.previousOutput != null) return false;
        this.remove();
        for (WireWidget wire : nextWires) {
            if (wire.removeWithoutInputs()) pos().editor().rootWidgets.remove(wire);
        }
        return true;
    }

    public void removeConnection() {
        List<WireWidget> nextWiresClone = new ArrayList<>(nextWires);
        List<WireWidget> previousWiresClone = new ArrayList<>(previousWires);
        this.remove();
        for (WireWidget wire : previousWiresClone) {
            if (this.type instanceof SignalType) {
                wire.removePrevious();
                pos().editor().rootWidgets.remove(wire);
            }
            else if (wire.removeWithoutOutputs()) pos().editor().rootWidgets.remove(wire);
        }

        for (WireWidget wire : nextWiresClone) {
            if (!(this.type instanceof SignalType)) {
                wire.removeNext();
                pos().editor().rootWidgets.remove(wire);
            }
            else if (wire.removeWithoutInputs()) pos().editor().rootWidgets.remove(wire);
        }
        pos().editor().rootWidgets.remove(this);
    }

    public List<NodeIOWidget> getInputs() {
        List<NodeIOWidget> list = new ArrayList<>();
        for (WireWidget wire : nextWires) {
            list.addAll(wire.getInputs(this));
        }

        if (previousOutput != null) {
            list.add(previousOutput);
            return list;
        }
        for (WireWidget wire : previousWires) {
            list.addAll(wire.getInputs(this));
        }
        return list;
    }

    private List<NodeIOWidget> getInputs(WireWidget prev) {
        List<NodeIOWidget> list = new ArrayList<>();
        for (WireWidget wire : nextWires) {
            if (wire == prev) continue;
            list.addAll(wire.getInputs(this));
        }

        if (previousOutput != null) {
            list.add(previousOutput);
            return list;
        }
        for (WireWidget wire : previousWires) {
            if (wire == prev) continue;
            list.addAll(wire.getInputs(this));
        }
        return list;
    }

    public List<NodeIOWidget> getOutputs() {
        List<NodeIOWidget> list = new ArrayList<>();
        for (WireWidget wire : previousWires) {
            list.addAll(wire.getOutputs(this));
        }

        if (nextInput != null) {
            list.add(nextInput);
            return list;
        }
        for (WireWidget wire : nextWires) {
            list.addAll(wire.getOutputs(this));
        }
        return list;
    }

    private List<NodeIOWidget> getOutputs(WireWidget prev) {
        List<NodeIOWidget> list = new ArrayList<>();
        for (WireWidget wire : previousWires) {
            if (wire == prev) continue;
            list.addAll(wire.getOutputs(this));
        }

        if (nextInput != null) {
            list.add(nextInput);
            return list;
        }
        for (WireWidget wire : nextWires) {
            if (wire == prev) continue;
            list.addAll(wire.getOutputs(this));
        }
        return list;
    }

    public WireType<?> type() {
        return type;
    }

    public List<WireWidget> splitWire(WidgetVec pos) {
        WireWidget w1;
        if (this.previousWires.isEmpty()) w1 = new WireWidget(pos, this.previousOutput, this.type(), pos);
        else w1 = new WireWidget(this.previousWires, this.type(), pos);
        pos.editor().rootWidgets.add(w1);
        for (WireWidget wireWidget1 : this.previousWires) {
            wireWidget1.nextWires.remove(this);
            wireWidget1.nextWires.add(w1);
        }
        WireWidget w2 = new WireWidget(w1, this.type(), this.line.to);
        pos.editor().rootWidgets.add(w2);
        w2.addNextWires(this.nextWires);
        for (WireWidget nextWire : this.nextWires) {
            nextWire.previousWires.remove(this);
            nextWire.previousWires.add(w2);
        }
        NodeIOWidget previousOutput = this.previousOutput;
        if (previousOutput != null) {
            w1.setPreviousOutput(previousOutput);
            previousOutput.connections.remove(this);
            previousOutput.connections.add(w1);
        }
        NodeIOWidget nextInput = this.nextInput;
        if (nextInput != null) {
            w2.setNextInput(nextInput);
            nextInput.connections.remove(this);
            nextInput.connections.add(w2);
        }
        this.arrows.forEach(TextWidget::remove);
        this.arrows.clear();
        this.line.remove();
        pos.editor().rootWidgets.remove(this);
        w1.update();
        w2.update();
        return List.of(w1, w2);
    }

    public boolean isValid() {
        return !getInputs().isEmpty() && !getOutputs().isEmpty();
    }

    public void connectNext(WireWidget wire) {
        this.nextWires.add(wire);
        wire.previousWires.add(this);
    }

    public void connectNext(NodeIOWidget node) {
        this.nextInput = node;
        node.connections.add(this);
    }

    public void connectPrevious(WireWidget wire) {
        this.previousWires.add(wire);
        wire.nextWires.add(this);
    }

    public void connectPrevious(NodeIOWidget node) {
        this.previousOutput = node;
        node.connections.add(this);
    }

    public void cleanup() {
        while (true) {
            if (previousWires.size() == 1 && previousWires.getFirst().nextWires.size() == 1 && sameDirection(previousWires.getFirst().line.from, previousWires.getFirst().line.to, line.from, line.to)) {
                combine(previousWires.getFirst(), true);
                continue;
            }
            if (nextWires.size() == 1 && nextWires.getFirst().previousWires.size() == 1 && sameDirection(nextWires.getFirst().line.from, nextWires.getFirst().line.to, line.from, line.to)) {
                combine(nextWires.getFirst(), false);
                continue;
            }
            break;
        }
        previousWires.forEach(WireWidget::cleanupPrevious);
        nextWires.forEach(WireWidget::cleanupNext);
    }

    private void cleanupPrevious() {
        while (true) {
            if (previousWires.size() == 1 && previousWires.getFirst().nextWires.size() == 1 && sameDirection(previousWires.getFirst().line.from, previousWires.getFirst().line.to, line.from, line.to)) {
                combine(previousWires.getFirst(), true);
                continue;
            }
            break;
        }
        previousWires.forEach(WireWidget::cleanupPrevious);
    }

    private void cleanupNext() {
        while (true) {
            if (nextWires.size() == 1 && nextWires.getFirst().previousWires.size() == 1 && sameDirection(nextWires.getFirst().line.from, nextWires.getFirst().line.to, line.from, line.to)) {
                combine(nextWires.getFirst(), false);
                continue;
            }
            break;
        }
        nextWires.forEach(WireWidget::cleanupNext);
    }

    private void combine(WireWidget wire, boolean isPrevious) {
        if (isPrevious) {
            this.previousWires.remove(wire);
            this.previousWires.addAll(wire.previousWires);
            this.previousWires.forEach(prevWire -> {
                prevWire.nextWires.remove(wire);
                prevWire.nextWires.add(this);
            });
            this.line.from = wire.line.from;
            if (wire.previousOutput != null) {
                this.previousOutput = wire.previousOutput;
                this.previousOutput.connections.remove(wire);
                this.previousOutput.removed(wire);
                wire.previousOutput = null;
                this.previousOutput.connections.add(this);
                this.previousOutput.connect(this);
            }
        } else {
            this.nextWires.remove(wire);
            this.nextWires.addAll(wire.nextWires);
            this.nextWires.forEach(prevWire -> {
                prevWire.previousWires.remove(wire);
                prevWire.previousWires.add(this);
            });
            this.line.to = wire.line.to;
            if (wire.nextInput != null) {
                Node.Varargs<?> varargs = wire.nextInput.input.varargsParent;
                if (varargs != null) varargs.ignoreUpdates = true;
                this.nextInput = wire.nextInput;
                this.nextInput.connections.remove(wire);
                this.nextInput.removed(wire);
                wire.nextInput = null;
                this.nextInput.connections.add(this);
                this.nextInput.connect(this);
                if (varargs != null) varargs.ignoreUpdates = false;
            }
        }
        this.update();
        wire.remove();
        pos().editor().rootWidgets.remove(wire);
    }

    public static boolean sameDirection(WidgetVec u, WidgetVec v, WidgetVec w, WidgetVec z) {
        double d1x = v.x() - u.x();
        double d1y = v.y() - u.y();
        double d2x = z.x() - w.x();
        double d2y = z.y() - w.y();

        boolean d1IsZero = (d1x == 0 && d1y == 0);
        boolean d2IsZero = (d2x == 0 && d2y == 0);
        if (d1IsZero || d2IsZero) {
            return true;
        }

        double crossProduct = d1x * d2y - d1y * d2x;
        double dotProduct = d1x * d2x + d1y * d2y;
        return crossProduct == 0 && dotProduct > 0;
    }

    public boolean lockWire(ServerPlayerEntity player) {
        List<Widget> widgets = pos().editor().lockWidgets(new ArrayList<>(getFullWire()), player);
        return widgets.isEmpty();
    }

    public void unlockWire(ServerPlayerEntity player) {
        pos().editor().unlockWidgets(new ArrayList<>(getFullWire()), player);
    }

    public Set<WireWidget> getFullWire() {
        Set<WireWidget> wires = new HashSet<>();
        wires.add(this);
        wires.addAll(previousWires);
        wires.addAll(nextWires);
        previousWires.forEach(wire -> wires.addAll(wire.getFullPreviousWires(this)));
        nextWires.forEach(wire -> wires.addAll(wire.getFullNextWires(this)));
        return wires;
    }

    private Set<WireWidget> getFullPreviousWires(WireWidget avoid) {
        Set<WireWidget> wires = new HashSet<>(previousWires);
        Set<WireWidget> nextWiresClone = new HashSet<>(nextWires);
        nextWiresClone.remove(avoid);
        wires.addAll(nextWiresClone);
        previousWires.forEach(wire -> wires.addAll(wire.getFullPreviousWires(this)));
        nextWires.forEach(wire -> {
            if (wire == avoid) return;
            wires.addAll(wire.getFullNextWires(this));
        });
        return wires;
    }

    private Set<WireWidget> getFullNextWires(WireWidget avoid) {
        Set<WireWidget> wires = new HashSet<>(nextWires);
        Set<WireWidget> previousWiresClone = new HashSet<>(previousWires);
        previousWiresClone.remove(avoid);
        wires.addAll(previousWiresClone);
        previousWires.forEach(wire -> {
            if (wire == avoid) return;
            wires.addAll(wire.getFullPreviousWires(this));
        });
        nextWires.forEach(wire -> wires.addAll(wire.getFullNextWires(this)));
        return wires;
    }

    @Override
    public List<Widget> getChildren() {
        return List.of();
    }

    @Override
    public boolean inBounds(WidgetVec pos) {
        WidgetVec min = line.from.min(line.to);
        WidgetVec max = line.from.max(line.to);
        return pos.x() + 1/16f >= min.x() && pos.x() <= max.x() + 1/16f && pos.y() + 1/16f >= min.y() && pos.y() <= max.y() + 1/16f;
    }

    @Override
    public boolean interact(CodeInteraction i) {
        if (!inBounds(i.pos())) return false;
        if (i.type() == CodeInteraction.Type.LEFT_CLICK) {
            List<NodeIOWidget> inputs = getInputs();
            List<NodeIOWidget> outputs = getOutputs();
            removeConnection();
            if (this.type == SignalType.INSTANCE && !outputs.getFirst().connections.isEmpty()) outputs.getFirst().connections.getFirst().cleanup();
            else if (!inputs.getFirst().connections.isEmpty()) inputs.getFirst().connections.getFirst().cleanup();
            return true;
        } else if (i.type() == CodeInteraction.Type.SWAP_HANDS) {
            if (type != SignalType.INSTANCE) {
                i.pos().editor().setAction(i.player(), new WireAction(this, i.pos().gridAligned(), i.player()));
                return true;
            }
        } else if (i.type() == CodeInteraction.Type.RIGHT_CLICK) {
            if (!previousWires.isEmpty() && !nextWires.isEmpty()) {
                boolean horizontal = line.from.x() == line.to.x();
                for (WireWidget wire : previousWires) {
                    if (horizontal && wire.line.from.x() == wire.line.to.x()) {
                        if (wire.previousWires.isEmpty() || wire.nextWires.isEmpty()) return false;
                    } else if (!horizontal && wire.line.from.y() == wire.line.to.y()) {
                        if (wire.previousWires.isEmpty() || wire.nextWires.isEmpty()) return false;
                    }
                }
                for (WireWidget wire : nextWires) {
                    if (horizontal && wire.line.from.x() == wire.line.to.x()) {
                        if (wire.previousWires.isEmpty() || wire.nextWires.isEmpty()) return false;
                    } else if (!horizontal && wire.line.from.y() == wire.line.to.y()) {
                        if (wire.previousWires.isEmpty() || wire.nextWires.isEmpty()) return false;
                    }
                }
                i.pos().editor().setAction(i.player(), new DragWireAction(this, i.pos().editor(), i.player()));
                return true;
            }
        }
        return false;
    }
}
