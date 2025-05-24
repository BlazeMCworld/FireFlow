package de.blazemcworld.fireflow.code.action;

import de.blazemcworld.fireflow.code.CodeEditor;
import de.blazemcworld.fireflow.code.CodeInteraction;
import de.blazemcworld.fireflow.code.EditOrigin;
import de.blazemcworld.fireflow.code.node.impl.function.FunctionInputsNode;
import de.blazemcworld.fireflow.code.node.impl.function.FunctionOutputsNode;
import de.blazemcworld.fireflow.code.widget.NodeIOWidget;
import de.blazemcworld.fireflow.code.widget.NodeWidget;
import de.blazemcworld.fireflow.code.widget.WidgetVec;
import de.blazemcworld.fireflow.code.widget.WireWidget;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DragNodeAction implements CodeAction {
    private final NodeWidget node;
    private final WidgetVec offset;
    private final List<NodeIOWidget> iowidgets;

    public DragNodeAction(NodeWidget node, WidgetVec offset, EditOrigin player) {
        this.node = node;
        this.offset = offset;

        offset.editor().lockWidget(node, player);
        node.borderColor(TextColor.fromFormatting(Formatting.AQUA));
        iowidgets = node.getIOWidgets();
        for (NodeIOWidget IOWidget : new ArrayList<>(iowidgets)) {
            for (WireWidget wire : new ArrayList<>(IOWidget.connections)) {
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
                    nw.lockWire(player);
                } else {
                    wire.lockWire(player);
                }
            }
        }
    }

    @Override
    public void tick(WidgetVec cursor, EditOrigin player) {
        node.pos(cursor.add(offset).gridAligned());
        node.update();
        for (NodeIOWidget ioWidget : iowidgets) {
            for (WireWidget wire : ioWidget.connections) {
                if (ioWidget.isInput()) {
                    wire.line.to = ioWidget.pos().sub(1 / 8f - 1 / 32f, 1 / 8f);
                    wire.line.from = new WidgetVec(cursor.editor(), wire.line.from.x(), ioWidget.pos().y() - 1 / 8f);
                    wire.previousWires.getFirst().line.to = new WidgetVec(cursor.editor(), wire.previousWires.getFirst().line.to.x(), ioWidget.pos().y() - 1 / 8f);
                    wire.previousWires.getFirst().update();
                } else {
                    wire.line.from = ioWidget.pos().sub(ioWidget.size().sub(0, 1 / 8f));
                    wire.line.to = new WidgetVec(cursor.editor(), wire.line.to.x(), ioWidget.pos().y() - 1 / 8f);
                    wire.nextWires.getFirst().line.from = new WidgetVec(cursor.editor(), wire.nextWires.getFirst().line.from.x(), ioWidget.pos().y() - 1 / 8f);
                    wire.nextWires.getFirst().update();
                }
                wire.update();
            }
        }
    }

    @Override
    public boolean interact(CodeInteraction i) {
        if (i.type() == CodeInteraction.Type.RIGHT_CLICK) i.pos().editor().stopAction(i.origin());
        if (i.type() == CodeInteraction.Type.SWAP_HANDS) {
            if (node.node instanceof FunctionInputsNode || node.node instanceof FunctionOutputsNode) return false;
            NodeWidget copy = new NodeWidget(node.pos(), node.node.copy());
            for (NodeIOWidget io : node.getInputs()) {
                List<NodeIOWidget> inputs = copy.getInputs();
                NodeIOWidget match = null;
                for (int j = inputs.size() - 1; j >= 0; j--) {
                    match = inputs.get(j);
                    if ((match.input.varargsParent == null || io.input.varargsParent == null) && Objects.equals(io.input.id, match.input.id)) break;
                    if (match.input.varargsParent != null && io.input.varargsParent != null && Objects.equals(io.input.varargsParent.id, match.input.varargsParent.id)) break;
                }
                if (match != null && io.input.inset != null) match.insetValue(io.input.inset);
            }
            copy.update();
            i.pos().editor().rootWidgets.add(copy);
            i.pos().editor().stopAction(i.origin());
            i.pos().editor().setAction(i.origin(), new DragNodeAction(copy, offset, i.origin()));
        }
        return true;
    }

    @Override
    public void stop(CodeEditor editor, EditOrigin player) {
        node.borderColor(TextColor.fromFormatting(Formatting.WHITE));
        editor.unlockWidget(node, player);
        for (NodeIOWidget IOWidget : iowidgets) {
            for (WireWidget wire : IOWidget.connections) {
                wire.unlockWire(player);
                wire.cleanup();
            }
        }

        editor.unlockWidget(node, player);
    }
}
