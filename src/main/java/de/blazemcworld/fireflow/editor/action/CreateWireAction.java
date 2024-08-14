package de.blazemcworld.fireflow.editor.action;

import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.editor.Bounds;
import de.blazemcworld.fireflow.editor.CodeEditor;
import de.blazemcworld.fireflow.editor.EditorAction;
import de.blazemcworld.fireflow.editor.Widget;
import de.blazemcworld.fireflow.editor.widget.*;
import de.blazemcworld.fireflow.node.ExtractionNode;
import de.blazemcworld.fireflow.node.NodeInput;
import de.blazemcworld.fireflow.node.NodeOutput;
import de.blazemcworld.fireflow.node.NodeCategory;
import de.blazemcworld.fireflow.value.SignalValue;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class CreateWireAction implements EditorAction {

    private NodeInput nodeInput = null;
    private NodeOutput nodeOutput = null;
    private final ButtonWidget origin;
    private final Player player;
    private final CodeEditor editor;
    private final Stack<LineWidget> lines = new Stack<>();

    public CreateWireAction(NodeInput input, ButtonWidget btn, Player player, CodeEditor editor) {
        this.nodeInput = input;
        this.origin = btn;
        this.player = player;
        this.editor = editor;
        lines.add(new LineWidget(editor.inst));
        lines.getFirst().color = input.getType().getColor();
    }

    public CreateWireAction(NodeOutput output, ButtonWidget btn, Player player, CodeEditor editor) {
        this.nodeOutput = output;
        this.origin = btn;
        this.player = player;
        this.editor = editor;
        lines.add(new LineWidget(editor.inst));
        lines.getFirst().color = output.getType().getColor();
    }

    @Override
    public void rightClick(Vec cursor) {
        Widget selected = editor.getWidget(player, cursor);
        if (selected instanceof NodeInputWidget other && nodeOutput != null) {
            if (other.input.type != nodeOutput.type) return;

            if (nodeOutput.type == SignalValue.INSTANCE) {
                ((NodeOutputWidget) origin).disconnect();
                nodeOutput.connectSignal(other.input);
            } else {
                other.disconnect();
                other.input.connectValue(nodeOutput);
            }

            List<Vec> relays = new ArrayList<>();
            for (LineWidget w : lines) relays.add(w.to);
            relays.removeLast();
            other.addWire(new WireWidget(editor.inst, other, (NodeOutputWidget) origin, relays.reversed()));

            editor.setAction(player, null);
            return;
        }
        if (selected instanceof NodeOutputWidget other && nodeInput != null) {
            if (other.output.type != nodeInput.type) return;

            if (nodeInput.type == SignalValue.INSTANCE) {
                other.disconnect();
                other.output.connectSignal(nodeInput);
            } else {
                ((NodeInputWidget) origin).disconnect();
                nodeInput.connectValue(other.output);
            }

            List<Vec> relays = new ArrayList<>();
            for (LineWidget w : lines) relays.add(w.to);
            relays.removeLast();
            ((NodeInputWidget) origin).addWire(new WireWidget(editor.inst, (NodeInputWidget) origin, other, relays));

            editor.setAction(player, null);
            return;
        }

        Vec end = lines.peek().to;
        lines.add(new LineWidget(editor.inst));
        lines.peek().from = end;
        if (nodeInput != null) {
            lines.peek().color = nodeInput.getType().getColor();
        } else {
            lines.peek().color = nodeOutput.getType().getColor();
        }
    }

    @Override
    public void leftClick(Vec cursor) {
        if (lines.size() <= 1) editor.setAction(player, null);
            else lines.pop().remove();
    }

    @Override
    public void swapItem(Vec cursor) {
        if (nodeOutput == null || nodeOutput.type == SignalValue.INSTANCE) return;
        NodeCategory category = NodeCategory.getExtractions(nodeOutput.type);
        if (category == null) return;

        CategoryWidget selector = new CategoryWidget(cursor, editor, category, Component.text("» " + category.name() + ":", NamedTextColor.YELLOW, TextDecoration.ITALIC));
        editor.widgets.add(selector);
        editor.setAction(player, null);

        List<Vec> relays = new ArrayList<>();
        for (LineWidget w : lines) relays.add(w.to);
        relays.removeLast();
        selector.selectCallback = (widget) -> {
            if (widget.node instanceof ExtractionNode extraction) {
                extraction.input.connectValue(nodeOutput);
                NodeOutputWidget outputWidget = (NodeOutputWidget) origin;
                widget.inputs.getFirst().addWire(new WireWidget(editor.inst, widget.inputs.getFirst(), outputWidget, relays));
            } else {
                FireFlow.LOGGER.error("Node {} is not an extraction node!", widget.node);
            }
        };
    }

    @Override
    public void tick(Vec cursor) {
        Bounds bounds = origin.bounds();
        Vec startPos = origin.position.add(0, 0.2, 0);
        if (nodeOutput != null) {
            startPos = startPos.add(-bounds.size().x() + 0.05, 0, 0);
        } else {
            startPos = startPos.add(-0.1, 0, 0);
        }
        lines.getFirst().from = startPos;
        lines.getFirst().update();
        lines.peek().to = cursor;
        lines.peek().update();
    }

    @Override
    public void stop() {
        for (LineWidget line : lines) {
            line.remove();
        }
    }
}
