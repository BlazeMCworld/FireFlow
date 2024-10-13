package de.blazemcworld.fireflow.code.widget;

import de.blazemcworld.fireflow.code.CodeEditor;
import de.blazemcworld.fireflow.code.Interaction;
import de.blazemcworld.fireflow.code.action.WireAction;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.WireType;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.InstanceContainer;

import java.util.ArrayList;
import java.util.List;

public class WireWidget implements Widget {

    public NodeIOWidget previousOutput;
    public final List<WireWidget> previousWires = new ArrayList<>();
    public final LineElement line = new LineElement();
    public final List<WireWidget> nextWires = new ArrayList<>();
    public NodeIOWidget nextInput;
    private final WireType<?> type;

    public WireWidget(NodeIOWidget previousOutput, WireType<?> type, Vec cursor) {
        if (!previousOutput.isInput()) this.previousOutput = previousOutput;
        else this.nextInput = previousOutput;
        if (previousOutput.isInput()) line.from = previousOutput.getPos().sub(1/8f-1/32f, 1/8f, 0);
        else line.from = previousOutput.getPos().sub(previousOutput.getSize().sub(1/8f, 1/8f, 0));
        line.to = cursor;
        line.color(previousOutput.color());
        this.type = type;
    }

    public WireWidget(WireWidget previousWire, WireType<?> type, Vec cursor) {
        this.previousWires.add(previousWire);
        line.from = previousWire.line.to;
        line.to = cursor;
        line.color(previousWire.line.color());
        this.type = type;
    }

    public WireWidget(WireWidget wire, WireType<?> type, Vec cursor, boolean isNext) {
        if (!isNext) {
            this.previousWires.add(wire);
            line.from = wire.line.to;
        } else {
            this.nextWires.add(wire);
            line.from = wire.line.from;
        }
        line.to = cursor;
        line.color(wire.line.color());
        this.type = type;
    }

    public WireWidget(List<WireWidget> previousWires, WireType<?> type, Vec cursor) {
        this.previousWires.addAll(previousWires);
        line.from = previousWires.getFirst().line.to;
        line.to = cursor;
        line.color(previousWires.getFirst().line.color());
        this.type = type;
    }

    @Override
    public void setPos(Vec pos) {
        if (Math.abs(pos.x() - line.from.x()) >= Math.abs(pos.y() - line.from.y())) pos = new Vec(pos.x(), line.from.y(), 0);
        else pos = new Vec(line.from.x(), pos.y(), 0);
        line.to = pos;
    }

    @Override
    public Vec getPos() {
        return new Vec(Math.max(line.from.x(), line.to.x()), Math.max(line.from.y(), line.to.y()), 0).add(1/16f, 1/16f, 0);
    }

    @Override
    public Vec getSize() {
        return getPos().sub(Math.min(line.from.x(), line.to.x()), Math.min(line.from.y(), line.to.y()), 0).add(1/16f, 1/16f, 0);
    }

    @Override
    public void update(InstanceContainer inst) {
        line.update(inst);
    }

    @Override
    public void remove() {
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

    public void removeAll(CodeEditor editor) {
        this.remove();
        for (WireWidget wire : previousWires) {
            wire.removeAll(editor);
            editor.rootWidgets.remove(wire);
        }
        editor.rootWidgets.remove(this);
    }

    @Override
    public boolean interact(Interaction i) {
        if (!inBounds(i.pos())) return false;
        if (i.type() == Interaction.Type.LEFT_CLICK) {
            removeConnection(i.editor());
            return true;
        } else if (i.type() == Interaction.Type.RIGHT_CLICK) {
            WireWidget w1;
            if (this.previousWires.isEmpty()) w1 = new WireWidget(this.previousOutput, this.type(), i.pos());
            else w1 = new WireWidget(this.previousWires, this.type(), i.pos());
            for (WireWidget wire : this.previousWires) {
                wire.nextWires.remove(this);
                wire.nextWires.add(w1);
            }
            if (this.previousOutput != null) {
                this.previousOutput.connections.remove(this);
                this.previousOutput.connections.add(w1);
            }
            WireWidget w2 = new WireWidget(w1, this.type(), this.line.to);
            w2.addNextWires(this.nextWires);

            WireWidget wire;
            if (this.type == SignalType.INSTANCE) {
                wire = new WireWidget(w2, this.type, i.pos(), true);
                w2.addPreviousWire(wire);
            } else {
                wire = new WireWidget(w1, this.type, i.pos());
                w1.addNextWire(wire);
            }
            i.editor().rootWidgets.add(wire);
            for (WireWidget nextWire : this.nextWires) {
                nextWire.previousWires.remove(this);
                nextWire.previousWires.add(w2);
            }
            if (this.nextInput != null) {
                w2.nextInput = this.nextInput;
                this.nextInput.connections.remove(this);
                this.nextInput.connections.add(w2);
            }

            w1.addNextWire(w2);
            this.remove();
            i.editor().rootWidgets.remove(this);
            i.editor().rootWidgets.add(w1);
            w1.update(i.editor().space.code);
            i.editor().rootWidgets.add(w2);
            w2.update(i.editor().space.code);
            i.editor().setAction(i.player(), new WireAction(wire, getPos().sub(i.pos()), this.type == SignalType.INSTANCE));
            return true;
        }
        return false;
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

    private void removeNext(CodeEditor editor) {
        this.remove();
        for (WireWidget wire : nextWires) {
            wire.removeNext(editor);
            editor.rootWidgets.remove(wire);
        }
    }

    private void removePrevious(CodeEditor editor) {
        this.remove();
        for (WireWidget wire : previousWires) {
            wire.removePrevious(editor);
            editor.rootWidgets.remove(wire);
        }
    }

    private boolean removeWithoutOutputs(CodeEditor editor) {
        if (!this.nextWires.isEmpty() || this.nextInput != null) return false;
        this.remove();
        for (WireWidget wire : previousWires) {
            if (wire.removeWithoutOutputs(editor)) editor.rootWidgets.remove(wire);
        }
        return true;
    }

    private boolean removeWithoutInputs(CodeEditor editor) {
        if (!this.previousWires.isEmpty() || this.previousOutput != null) return false;
        this.remove();
        for (WireWidget wire : nextWires) {
            if (wire.removeWithoutInputs(editor)) editor.rootWidgets.remove(wire);
        }
        return true;
    }

    public void removeConnection(CodeEditor editor) {
        this.remove();
        for (WireWidget wire : previousWires) {
            if (this.type instanceof SignalType) {
                wire.removePrevious(editor);
                editor.rootWidgets.remove(wire);
            }
            else if (wire.removeWithoutOutputs(editor)) editor.rootWidgets.remove(wire);
        }

        for (WireWidget wire : nextWires) {
            if (!(this.type instanceof SignalType)) {
                wire.removeNext(editor);
                editor.rootWidgets.remove(wire);
            }
            else if (wire.removeWithoutInputs(editor)) editor.rootWidgets.remove(wire);
        }
        editor.rootWidgets.remove(this);
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

    @Override
    public List<Widget> getChildren() {
        return null;
    }

    public List<WireWidget> splitWire(CodeEditor editor, Vec pos) {
        WireWidget w1;
        if (this.previousWires.isEmpty()) w1 = new WireWidget(this.previousOutput, this.type(), pos);
        else w1 = new WireWidget(this.previousWires, this.type(), pos);
        for (WireWidget wireWidget1 : this.previousWires) {
            wireWidget1.nextWires.remove(this);
            wireWidget1.nextWires.add(w1);
        }
        if (this.previousOutput != null) {
            w1.setPreviousOutput(this.previousOutput);
            this.previousOutput.connections.remove(this);
            this.previousOutput.connections.add(w1);
        }
        WireWidget w2 = new WireWidget(w1, this.type(), this.line.to);
        w2.addNextWires(this.nextWires);
        for (WireWidget nextWire : this.nextWires) {
            nextWire.previousWires.remove(this);
            nextWire.previousWires.add(w2);
        }
        if (this.nextInput != null) {
            w2.setNextInput(this.nextInput);
            this.nextInput.connections.remove(this);
            this.nextInput.connections.add(w2);
        }
        w1.addNextWire(w2);
        this.remove();
        editor.rootWidgets.remove(this);
        editor.rootWidgets.add(w1);
        w1.update(editor.space.code);
        editor.rootWidgets.add(w2);
        w2.update(editor.space.code);
        return List.of(w1, w2);
    }
}
