package de.blazemcworld.fireflow.code.action;

import de.blazemcworld.fireflow.code.CodeEditor;
import de.blazemcworld.fireflow.code.Interaction;
import de.blazemcworld.fireflow.code.widget.NodeIOWidget;
import de.blazemcworld.fireflow.code.widget.NodeWidget;
import de.blazemcworld.fireflow.code.widget.Widget;
import de.blazemcworld.fireflow.code.widget.WireWidget;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DragSelectionAction implements Action {
    private final Map<NodeWidget, Vec> nodeWidgets = new java.util.HashMap<>();
    private final Map<WireWidget, List<Vec>> wireWidgets = new java.util.HashMap<>();
    private final List<NodeIOWidget> ioWidgets = new ArrayList<>();
    private final Vec offset;

    public DragSelectionAction(List<Widget> widgets, Vec offset, CodeEditor editor) {
        this.offset = offset;

        for (Widget w : widgets) {
            if (w instanceof NodeWidget nodeWidget) {
                nodeWidget.borderColor(NamedTextColor.AQUA);
                nodeWidgets.put(nodeWidget, nodeWidget.getPos());
                for (NodeIOWidget IOWidget : new ArrayList<>(nodeWidget.getIOWidgets())) {
                    for (WireWidget wire : new ArrayList<>(IOWidget.connections)) {
                        if (widgets.contains(wire)) continue;
                        if (!ioWidgets.contains(IOWidget)) ioWidgets.add(IOWidget);
                        List<WireWidget> prevWires;
                        if (IOWidget.isInput()) prevWires = wire.previousWires;
                        else prevWires = wire.nextWires;
                        if (prevWires.size() != 1 || prevWires.getFirst().line.to.y() == prevWires.getFirst().line.from.y()) {
                            Vec mid = wire.line.from.add(wire.line.to).div(2);
                            List<WireWidget> splitWires = wire.splitWire(editor, mid);
                            WireWidget nw = new WireWidget(splitWires.getFirst(), wire.type(), mid);
                            nw.addNextWire(splitWires.getLast());
                            nw.setPos(mid);
                            splitWires.getFirst().nextWires.remove(splitWires.getLast());
                            splitWires.getLast().addPreviousWire(nw);
                            splitWires.getLast().previousWires.remove(splitWires.getFirst());
                            editor.rootWidgets.add(nw);
                            nw.update(editor.space.code);
                        }
                    }
                }
            } else if (w instanceof WireWidget wire) {
                wireWidgets.put(wire, List.of(wire.line.from, wire.line.to));
            }
        }
    }

    @Override
    public void tick(Vec cursor, CodeEditor editor, Player player) {
        Vec newPos = cursor.sub(offset).mul(8).apply(Vec.Operator.CEIL).div(8).withZ(0);
        nodeWidgets.forEach((nodeWidget, pos) -> {
            nodeWidget.setPos(pos.add(newPos));
            nodeWidget.update(editor.space.code);
        });
        wireWidgets.forEach((wire, points) -> {
            wire.line.from = points.get(0).add(newPos);
            wire.line.to = points.get(1).add(newPos);
            wire.update(editor.space.code);
        });

        for (NodeIOWidget IOWidget : ioWidgets) {
            for (WireWidget wire : IOWidget.connections) {
                if (IOWidget.isInput()) {
                    wire.line.to = IOWidget.getPos().sub(1/8f-1/32f, 1/8f, 0);
                    wire.line.from = new Vec(wire.line.from.x(), IOWidget.getPos().y()- 1/8f, wire.line.from.z());
                    wire.previousWires.getFirst().line.to = new Vec(wire.previousWires.getFirst().line.to.x(), IOWidget.getPos().y()- 1/8f, wire.previousWires.getFirst().line.to.z());
                    wire.previousWires.getFirst().update(editor.space.code);
                } else {
                    wire.line.from = IOWidget.getPos().sub(IOWidget.getSize().sub(1/8f, 1/8f, 0));
                    wire.line.to = new Vec(wire.line.to.x(), IOWidget.getPos().y()- 1/8f, wire.line.to.z());
                    wire.nextWires.getFirst().line.from = new Vec(wire.nextWires.getFirst().line.from.x(), IOWidget.getPos().y()- 1/8f, wire.nextWires.getFirst().line.from.z());
                    wire.nextWires.getFirst().update(editor.space.code);
                }
                wire.update(editor.space.code);
            }
        }
    }

    @Override
    public void interact(Interaction i) {
        if (i.type() == Interaction.Type.RIGHT_CLICK) i.editor().stopAction(i.player());
        else if (i.type() == Interaction.Type.LEFT_CLICK) {
            nodeWidgets.forEach((nodeWidget, pos) -> {
                nodeWidget.setPos(pos);
                nodeWidget.update(i.editor().space.code);
            });
            wireWidgets.forEach((wire, points) -> {
                wire.line.from = points.get(0);
                wire.line.to = points.get(1);
                wire.update(i.editor().space.code);
            });

            for (NodeIOWidget IOWidget : ioWidgets) {
                for (WireWidget wire : IOWidget.connections) {
                    if (IOWidget.isInput()) {
                        wire.line.to = IOWidget.getPos().sub(1/8f-1/32f, 1/8f, 0);
                        wire.line.from = new Vec(wire.line.from.x(), IOWidget.getPos().y()- 1/8f, wire.line.from.z());
                        wire.previousWires.getFirst().line.to = new Vec(wire.previousWires.getFirst().line.to.x(), IOWidget.getPos().y()- 1/8f, wire.previousWires.getFirst().line.to.z());
                        wire.previousWires.getFirst().update(i.editor().space.code);
                    } else {
                        wire.line.from = IOWidget.getPos().sub(IOWidget.getSize().sub(1/8f, 1/8f, 0));
                        wire.line.to = new Vec(wire.line.to.x(), IOWidget.getPos().y()- 1/8f, wire.line.to.z());
                        wire.nextWires.getFirst().line.from = new Vec(wire.nextWires.getFirst().line.from.x(), IOWidget.getPos().y()- 1/8f, wire.nextWires.getFirst().line.from.z());
                        wire.nextWires.getFirst().update(i.editor().space.code);
                    }
                    wire.update(i.editor().space.code);
                }
            }
            i.editor().stopAction(i.player());
        } else if (i.type() == Interaction.Type.SWAP_HANDS) {
            i.editor().stopAction(i.player());
            List<Widget> widgets = new ArrayList<>(nodeWidgets.keySet());
            widgets.addAll(wireWidgets.keySet());
            i.editor().setAction(i.player(), new CopySelectionAction(widgets, i.pos(), i.editor()));
        }
    }

    @Override
    public void stop(CodeEditor editor, Player player) {
        nodeWidgets.forEach((nodeWidget, pos) -> nodeWidget.borderColor(NamedTextColor.WHITE));
        wireWidgets.forEach((wire, points) -> wire.cleanup(editor));
        for (NodeIOWidget IOWidget : ioWidgets) {
            for (WireWidget wire : IOWidget.connections) {
                wire.cleanup(editor);
            }
        }
    }
}
