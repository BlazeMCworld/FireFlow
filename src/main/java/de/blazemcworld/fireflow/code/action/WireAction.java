package de.blazemcworld.fireflow.code.action;

import de.blazemcworld.fireflow.code.CodeEditor;
import de.blazemcworld.fireflow.code.Interaction;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.widget.NodeIOWidget;
import de.blazemcworld.fireflow.code.widget.Widget;
import de.blazemcworld.fireflow.code.widget.WireWidget;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class WireAction implements Action {
    private WireWidget wire;
    private final Vec offset;
    private final boolean needOutput;

    public WireAction(WireWidget wire, Vec offset) {
        this.wire = wire;
        this.offset = offset;
        this.needOutput = false;
    }

    public WireAction(WireWidget wire, Vec offset, boolean needOutput) {
        this.wire = wire;
        this.offset = offset;
        this.needOutput = needOutput;
    }

    @Override
    public void tick(Vec cursor, CodeEditor editor, Player player) {
        wire.setPos(cursor);
        wire.update(editor.space.code);
    }

    @Override
    public void interact(Interaction i) {
        if (i.type() == Interaction.Type.RIGHT_CLICK) {
            for (Widget widget : new HashSet<>(i.editor().rootWidgets)) {
                if (widget.getWidget(wire.line.to) instanceof NodeIOWidget nodeIOWidget) {
                    if (nodeIOWidget.isInput() == needOutput) return;
                    if (nodeIOWidget.type() != wire.type()) return;
                    if (nodeIOWidget.isInput() && !nodeIOWidget.connections.isEmpty() && nodeIOWidget.type() != SignalType.INSTANCE) {
                        for (WireWidget wireWidget : new ArrayList<>(nodeIOWidget.connections)) {
                            wireWidget.removeConnection(i.editor());
                        }
                    }
                    if (nodeIOWidget.isInput()) wire.setPos(nodeIOWidget.getPos().sub(1/8f-1/32f, 1/8f, 0));
                    else wire.setPos(nodeIOWidget.getPos().sub(nodeIOWidget.getSize().sub(1/8f, 1/8f, 0)));
                    if (!needOutput) wire.setNextInput(nodeIOWidget);
                    else {
                        Vec temp = wire.line.to;
                        wire.line.to = wire.line.from;
                        wire.line.from = temp;
                        wire.setPreviousOutput(nodeIOWidget);
                    }
                    wire.update(i.editor().space.code);
                    nodeIOWidget.connections.add(wire);
                    if (wire.type() == SignalType.INSTANCE) {
                        NodeIOWidget input = wire.getInputs().getFirst();
                        NodeIOWidget output = wire.getOutputs().getFirst();
                        for (WireWidget wireWidget : new ArrayList<>(input.connections)) {
                            if (!wireWidget.getOutputs().contains(output)) {
                                input.connections.remove(wireWidget);
                                input.removed(wireWidget);
                                wireWidget.removeConnection(i.editor());
                            }
                        }
                    }
                    for (NodeIOWidget nodeIO : wire.getInputs()) {
                        nodeIO.connect(wire);
                    }
                    for (NodeIOWidget nodeIO : wire.getOutputs()) {
                        nodeIO.connect(wire);
                    }
                    wire = null;
                    i.editor().stopAction(i.player());
                    return;
                } else if
                (
                    widget instanceof WireWidget wireWidget
                    && wireWidget != wire
                    && wire.type() == wireWidget.type()
                    && wireWidget.inBounds(wire.line.to)
                ) {
                    if (wire.type() == SignalType.INSTANCE) {
                        if (needOutput) return;
                        if (wireWidget.getInputs().contains(wire.getInputs().getFirst())) return;
                    } else {
                        if (!needOutput) return;
                        if (wireWidget.getOutputs().contains(wire.getOutputs().getFirst())) return;
                    }
                    List<WireWidget> splitWires = wireWidget.splitWire(i.editor(), wire.line.to);
                    if (needOutput) splitWires.getFirst().addNextWire(wire);
                    else splitWires.getLast().addPreviousWire(wire);
                    if (wire.type() == SignalType.INSTANCE) {
                        NodeIOWidget input = wire.getInputs().getFirst();
                        for (WireWidget WW : new ArrayList<>(input.connections)) {
                            if (!WW.getOutputs().isEmpty()) {
                                input.connections.remove(WW);
                                input.removed(WW);
                                WW.removeConnection(i.editor());
                            }
                        }
                    }
                    if (!needOutput) wire.addNextWire(splitWires.getLast());
                    else wire.addPreviousWire(splitWires.getFirst());
                    wire.update(i.editor().space.code);
                    for (NodeIOWidget nodeIO : wire.getInputs()) {
                        nodeIO.connect(wire);
                    }
                    for (NodeIOWidget nodeIO : wire.getOutputs()) {
                        nodeIO.connect(wire);
                    }
                    wire = null;
                    i.editor().stopAction(i.player());
                    return;
                }
            }

            WireWidget w;
            if (needOutput) {
                Vec temp = wire.line.to;
                wire.line.to = wire.line.from;
                wire.line.from = temp;
                w = new WireWidget(wire, wire.type(), i.pos().add(offset), true);
                wire.addPreviousWire(w);
            } else {
                w = new WireWidget(wire, wire.type(), i.pos().add(offset));
                wire.addNextWire(w);
            }
            i.editor().rootWidgets.add(w);
            wire.update(i.editor().space.code);
            this.wire = w;
        } else if (i.type() == Interaction.Type.LEFT_CLICK) {
            if ((!needOutput && wire.previousWires.isEmpty()) || (needOutput && wire.nextWires.isEmpty())) {
                i.editor().stopAction(i.player());
            } else {
                if (
                    !needOutput &&
                    (!wire.previousWires.getLast().nextWires.isEmpty() ||
                    wire.previousWires.getLast().nextInput != null ||
                    !wire.previousWires.getLast().previousWires.isEmpty() ||
                    wire.previousWires.getLast().previousOutput != null) &&
                    !(wire.previousWires.getLast().nextWires.size() == 1 && wire.previousWires.getLast().nextWires.contains(wire))
                ) {
                    i.editor().stopAction(i.player());
                    return;
                } else if (
                    needOutput &&
                    (!wire.nextWires.getLast().previousWires.isEmpty() ||
                    wire.nextWires.getLast().previousOutput != null ||
                    !wire.nextWires.getLast().nextWires.isEmpty() ||
                    wire.nextWires.getLast().nextInput != null) &&
                    !(wire.nextWires.getLast().previousWires.size() == 1 && wire.nextWires.getLast().previousWires.contains(wire))
                ) {
                    i.editor().stopAction(i.player());
                    return;
                }
                WireWidget w;
                if (!needOutput) w = wire.previousWires.getLast();
                else {
                    w = wire.nextWires.getLast();
                    Vec temp = w.line.to;
                    w.line.to = w.line.from;
                    w.line.from = temp;
                }
                wire.remove();
                i.editor().rootWidgets.remove(wire);
                wire = w;
            }
        }
    }

    @Override
    public void stop(CodeEditor editor, Player player) {
        if (!needOutput) {
            if (wire == null || wire.nextInput != null) return;
        } else {
            if (wire == null || wire.previousOutput != null) return;
        }
        wire.removeConnection(editor);
        editor.rootWidgets.remove(wire);
        editor.unlockWidgets(player);
    }
}
