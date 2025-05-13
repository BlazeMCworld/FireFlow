package de.blazemcworld.fireflow.code.action;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.impl.function.FunctionInputsNode;
import de.blazemcworld.fireflow.code.node.impl.function.FunctionOutputsNode;
import de.blazemcworld.fireflow.code.widget.*;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;

public class CopySelectionAction implements CodeAction {
    List<Widget> widgets = new ArrayList<>();
    WidgetVec offset;

    public CopySelectionAction(List<Widget> widgetList, WidgetVec offset) {
        this.offset = offset;
        List<Widget> widgets = new ArrayList<>(widgetList.stream().filter(widget -> {
            if (widget instanceof NodeWidget n) {
                if (n.node instanceof FunctionInputsNode) return false;
                if (n.node instanceof FunctionOutputsNode) return false;
                return true;
            }
            return widget instanceof WireWidget;
        }).toList());

        HashMap<NodeWidget, NodeWidget> oldToNewNodes = new HashMap<>();
        HashMap<WireWidget, WireWidget> oldToNewWires = new HashMap<>();

        for (Widget w : widgets) {
            if (w instanceof NodeWidget nodeWidget) {
                Node nodeCopy = nodeWidget.node.copy();
                NodeWidget nodeWidgetCopy = new NodeWidget(nodeWidget.pos(), nodeCopy);
                offset.editor().rootWidgets.add(nodeWidgetCopy);
                nodeWidgetCopy.update();
                oldToNewNodes.put(nodeWidget, nodeWidgetCopy);
                this.widgets.add(nodeWidgetCopy);
            }
        }

        HashSet<Widget> widgetsHashset = new HashSet<>(widgets);
        for (Widget w : widgets) {
            if (w instanceof WireWidget wireWidget) {
                List<NodeWidget> inputs = wireWidget.getInputs().stream().map(widget -> widget.parent).toList();
                List<NodeWidget> outputs = wireWidget.getOutputs().stream().map(widget -> widget.parent).toList();
                if (!widgetsHashset.containsAll(inputs) || !widgetsHashset.containsAll(outputs)) continue;
                WireWidget wireWidgetCopy = new WireWidget(wireWidget.line.from, wireWidget.type(), wireWidget.line.to);
                offset.editor().rootWidgets.add(wireWidgetCopy);
                wireWidgetCopy.update();
                oldToNewWires.put(wireWidget, wireWidgetCopy);
                this.widgets.add(wireWidgetCopy);
            }
        }

        for (Widget w : widgets) {
            if (w instanceof WireWidget wireWidget) {
                if (oldToNewWires.get(wireWidget) == null) continue;
                WireWidget wireWidgetCopy = oldToNewWires.get(wireWidget);
                wireWidget.previousWires.forEach(wire -> wireWidgetCopy.addPreviousWire(oldToNewWires.get(wire)));
                wireWidget.nextWires.forEach(wire -> wireWidgetCopy.addNextWire(oldToNewWires.get(wire)));
                wireWidgetCopy.update();
            }
        }

        List<Node.Varargs<?>> varargs = new ArrayList<>();
        for (Widget w : widgets) {
            if (w instanceof NodeWidget nodeWidget) {
                NodeWidget nodeWidgetCopy = oldToNewNodes.get(nodeWidget);
                for (NodeIOWidget io : nodeWidget.getInputs()) {
                    List<NodeIOWidget> inputs = nodeWidgetCopy.getInputs();
                    NodeIOWidget match = null;
                    for (int j = inputs.size() - 1; j >= 0; j--) {
                        match = inputs.get(j);
                        if ((match.input.varargsParent == null || io.input.varargsParent == null) && Objects.equals(io.input.id, match.input.id)) break;
                        if (match.input.varargsParent != null && io.input.varargsParent != null && Objects.equals(io.input.varargsParent.id, match.input.varargsParent.id)) break;
                    }
                    if (match == null) continue;
                    if (match.input.varargsParent != null && !varargs.contains(match.input.varargsParent)) {
                        varargs.add(match.input.varargsParent);
                        match.input.varargsParent.ignoreUpdates = true;
                    }
                    if (io.input.inset != null) match.insetValue(io.input.inset);
                    else {
                        for (WireWidget wire : io.connections) {
                            if (oldToNewWires.get(wire) == null) return;
                            match.connections.add(oldToNewWires.get(wire));
                            oldToNewWires.get(wire).setNextInput(match);
                        }
                    }
                    if (match.input.varargsParent != null && (io.input.inset != null || io.input.connected != null)) {
                        match.input.varargsParent.addInput(UUID.randomUUID().toString());
                        match.parent.refreshInputs();
                    }
                }
                for (int i = 0; i < nodeWidget.getOutputs().size(); i++) {
                    NodeIOWidget io = nodeWidget.getOutputs().get(i);
                    NodeIOWidget ioCopy = nodeWidgetCopy.getOutputs().get(i);
                    for (WireWidget wire : io.connections) {
                        if (oldToNewWires.get(wire) == null) return;
                        ioCopy.connections.add(oldToNewWires.get(wire));
                        oldToNewWires.get(wire).setPreviousOutput(ioCopy);
                    }
                }
            }
        }

        for (Widget w : widgets) {
            if (w instanceof NodeWidget nodeWidget) {
                NodeWidget nodeWidgetCopy = oldToNewNodes.get(nodeWidget);
                nodeWidgetCopy.getInputs().forEach(io -> io.connections.forEach(io::connect));
                nodeWidgetCopy.getOutputs().forEach(io -> io.connections.forEach(io::connect));
                nodeWidgetCopy.update();
            }
        }
        for (Node.Varargs<?> vararg : varargs) {
            vararg.ignoreUpdates = false;
            vararg.update();
        }
    }

    @Override
    public void tick(WidgetVec cursor, ServerPlayerEntity player) {
        cursor.editor().stopAction(player);
        if (widgets.isEmpty()) return;
        cursor.editor().lockWidgets(widgets, player);
        cursor.editor().setAction(player, new DragSelectionAction(widgets, offset, player));
    }
}
