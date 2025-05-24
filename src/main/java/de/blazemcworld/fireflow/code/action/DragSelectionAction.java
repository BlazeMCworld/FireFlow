package de.blazemcworld.fireflow.code.action;

import de.blazemcworld.fireflow.code.CodeEditor;
import de.blazemcworld.fireflow.code.CodeInteraction;
import de.blazemcworld.fireflow.code.EditOrigin;
import de.blazemcworld.fireflow.code.widget.*;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DragSelectionAction implements CodeAction {
    private final Map<NodeWidget, WidgetVec> nodeWidgets = new java.util.HashMap<>();
    private final Map<WireWidget, List<WidgetVec>> wireWidgets = new java.util.HashMap<>();
    private final List<NodeIOWidget> ioWidgets = new ArrayList<>();
    private final WidgetVec offset;

    public DragSelectionAction(List<Widget> widgets, WidgetVec offset, EditOrigin player) {
        offset.editor().lockWidgets(widgets, player);
        this.offset = offset;
        for (Widget w : widgets) {
            if (w instanceof NodeWidget nodeWidget) {
                nodeWidget.borderColor(TextColor.fromFormatting(Formatting.AQUA));
                nodeWidgets.put(nodeWidget, nodeWidget.pos());
                for (NodeIOWidget IOWidget : new ArrayList<>(nodeWidget.getIOWidgets())) {
                    for (WireWidget wire : new ArrayList<>(IOWidget.connections)) {
                        if (widgets.contains(wire)) continue;
                        if (!ioWidgets.contains(IOWidget)) ioWidgets.add(IOWidget);
                        List<WireWidget> prevWires;
                        if (IOWidget.isInput()) prevWires = wire.previousWires;
                        else prevWires = wire.nextWires;
                        if (prevWires.size() != 1 || prevWires.getFirst().line.to.y() == prevWires.getFirst().line.from.y()) {
                            WidgetVec mid = wire.line.from.add(wire.line.to).div(2);
                            List<WireWidget> splitWires = wire.splitWire(mid);
                            WireWidget nw = new WireWidget(splitWires.getFirst(), wire.type(), mid);
                            nw.addNextWire(splitWires.getLast());
                            nw.pos(mid);
                            splitWires.getFirst().nextWires.remove(splitWires.getLast());
                            splitWires.getLast().addPreviousWire(nw);
                            splitWires.getLast().previousWires.remove(splitWires.getFirst());
                            offset.editor().rootWidgets.add(nw);
                            nw.update();
                        }
                    }
                }
            } else if (w instanceof WireWidget wire) {
                wireWidgets.put(wire, List.of(wire.line.from, wire.line.to));
            }
        }
    }

    @Override
    public void tick(WidgetVec cursor, EditOrigin player) {
        WidgetVec newPos = cursor.sub(offset).gridAligned();
        nodeWidgets.forEach((nodeWidget, pos) -> {
            nodeWidget.pos(pos.add(newPos));
            nodeWidget.update();
        });
        wireWidgets.forEach((wire, points) -> {
            wire.line.from = points.get(0).add(newPos);
            wire.line.to = points.get(1).add(newPos);
            wire.update();
        });

        for (NodeIOWidget io : ioWidgets) {
            for (WireWidget wire : io.connections) {
                if (io.isInput()) {
                    wire.line.to = io.pos().sub(1 / 8f - 1 / 32f, 1 / 8f);
                    wire.line.from = new WidgetVec(cursor.editor(), wire.line.from.x(), io.pos().y() - 1 / 8f);
                    wire.previousWires.getFirst().line.to = new WidgetVec(cursor.editor(), wire.previousWires.getFirst().line.to.x(), io.pos().y() - 1 / 8f);
                    wire.previousWires.getFirst().update();
                } else {
                    wire.line.from = io.pos().sub(io.size().sub(0, 1 / 8f));
                    wire.line.to = new WidgetVec(io.pos().editor(), wire.line.to.x(), io.pos().y() - 1 / 8f);
                    wire.nextWires.getFirst().line.from = new WidgetVec(cursor.editor(), wire.nextWires.getFirst().line.from.x(), io.pos().y() - 1 / 8f);
                    wire.nextWires.getFirst().update();
                }
                wire.update();
            }
        }
    }

    @Override
    public boolean interact(CodeInteraction i) {
        if (i.type() == CodeInteraction.Type.RIGHT_CLICK) {
            i.pos().editor().stopAction(i.origin());
            return true;
        }
        else if (i.type() == CodeInteraction.Type.LEFT_CLICK) {
            nodeWidgets.forEach((nodeWidget, pos) -> {
                nodeWidget.pos(pos);
                nodeWidget.update();
            });
            wireWidgets.forEach((wire, points) -> {
                wire.line.from = points.get(0);
                wire.line.to = points.get(1);
                wire.update();
            });

            CodeEditor editor = i.pos().editor();
            for (NodeIOWidget io : ioWidgets) {
                for (WireWidget wire : io.connections) {
                    if (io.isInput()) {
                        wire.line.to = io.pos().sub(1 / 8f - 1 / 32f, 1 / 8f);
                        wire.line.from = new WidgetVec(editor, wire.line.from.x(), io.pos().y() - 1 / 8f);
                        wire.previousWires.getFirst().line.to = new WidgetVec(editor, wire.previousWires.getFirst().line.to.x(), io.pos().y() - 1 / 8f);
                        wire.previousWires.getFirst().update();
                    } else {
                        wire.line.from = io.pos().sub(io.size().sub(0, 1 / 8f));
                        wire.line.to = new WidgetVec(editor, wire.line.to.x(), io.pos().y() - 1 / 8f);
                        wire.nextWires.getFirst().line.from = new WidgetVec(editor, wire.nextWires.getFirst().line.from.x(), io.pos().y() - 1 / 8f);
                        wire.nextWires.getFirst().update();
                    }
                    wire.update();
                }
            }
            i.pos().editor().stopAction(i.origin());
            return true;
        } else if (i.type() == CodeInteraction.Type.SWAP_HANDS) {
            i.pos().editor().stopAction(i.origin());
            List<Widget> widgets = new ArrayList<>(nodeWidgets.keySet());
            widgets.addAll(wireWidgets.keySet());
            i.pos().editor().setAction(i.origin(), new CopySelectionAction(widgets, i.pos()));
            return true;
        }
        return false;
    }

    @Override
    public void stop(CodeEditor editor, EditOrigin player) {
        nodeWidgets.forEach((nodeWidget, pos) -> nodeWidget.borderColor(TextColor.fromFormatting(Formatting.WHITE)));
        wireWidgets.forEach((wire, points) -> wire.cleanup());
        for (NodeIOWidget IOWidget : ioWidgets) {
            for (WireWidget wire : IOWidget.connections) {
                wire.cleanup();
            }
        }

        List<Widget> widgets = new ArrayList<>(nodeWidgets.keySet());
        widgets.addAll(wireWidgets.keySet());
        editor.unlockWidgets(widgets, player);
    }
}
