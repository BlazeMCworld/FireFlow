package de.blazemcworld.fireflow.code.widget;

import de.blazemcworld.fireflow.code.CodeInteraction;
import de.blazemcworld.fireflow.code.action.DragNodeAction;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.impl.function.FunctionCallNode;
import de.blazemcworld.fireflow.code.node.impl.function.FunctionInputsNode;
import de.blazemcworld.fireflow.code.node.impl.function.FunctionOutputsNode;
import de.blazemcworld.fireflow.code.type.SignalType;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class NodeWidget extends Widget {

    public final Node node;
    private final BorderWidget<VerticalContainerWidget> root;
    private final VerticalContainerWidget inputArea;
    private boolean refreshingInputs = false;

    public NodeWidget(WidgetVec pos, Node node) {
        super(pos);
        this.node = node;

        VerticalContainerWidget main = new VerticalContainerWidget(pos);
        main.align = VerticalContainerWidget.Align.CENTER;
        HorizontalContainerWidget title = new HorizontalContainerWidget(pos, new ItemWidget(pos, node.icon), new TextWidget(pos, Text.literal(node.name)));

        if (!(node instanceof FunctionInputsNode || node instanceof FunctionOutputsNode || node instanceof FunctionCallNode)) {
            ButtonWidget helpButton = new ButtonWidget(pos, Text.literal(" ?").formatted(Formatting.GRAY));
            helpButton.handler = interaction -> {
                if (interaction.type() != CodeInteraction.Type.RIGHT_CLICK) return false;
                interaction.player().sendMessage(Text.literal(node.description).formatted(Formatting.YELLOW));
                return true;
            };
            title.widgets.add(helpButton);
        }

        main.widgets.add(title);

        HorizontalContainerWidget ioArea = new HorizontalContainerWidget(pos);
        main.widgets.add(ioArea);

        inputArea = new VerticalContainerWidget(pos);
        ioArea.widgets.add(inputArea);

        SpacingWidget spacing = new SpacingWidget(pos, new WidgetVec(pos.editor(), 1 / 8f, 0));
        ioArea.widgets.add(spacing);

        for (Node.Input<?> input : node.inputs) {
            inputArea.widgets.add(new NodeIOWidget(pos, this, input));
        }

        VerticalContainerWidget outputArea = new VerticalContainerWidget(pos);
        ioArea.widgets.add(outputArea);
        outputArea.align = VerticalContainerWidget.Align.RIGHT;

        for (Node.Output<?> output : node.outputs) {
            outputArea.widgets.add(new NodeIOWidget(pos, this, output));
        }

        double needed = Math.max(0, title.size().x() - ioArea.size().x());
        spacing.size = spacing.size.withX(spacing.size.x() + Math.ceil(needed * 8) / 8);
        root = new BorderWidget<>(main);
        root.backgroundColor(0x99001100);
    }

    @Override
    public WidgetVec size() {
        return root.size();
    }

    @Override
    public void update() {
        refreshInputs();
        root.pos(pos());
        root.update();
    }

    public void refreshInputs() {
        if (refreshingInputs) return;

        boolean didRemove = inputArea.widgets.removeIf(w -> {
            if (w instanceof NodeIOWidget io && !node.inputs.contains(io.input)) {
                io.remove();
                return true;
            }
            return false;
        });

        for (int i = 0; i < node.inputs.size(); i++) {
            Node.Input<?> input = node.inputs.get(i);
            if (i < inputArea.widgets.size() && inputArea.widgets.get(i) instanceof NodeIOWidget io && io.input == input) {
                continue;
            }
            NodeIOWidget io = new NodeIOWidget(pos(), this, input);
            inputArea.widgets.add(i, io);
        }
        if (pos().editor() != null) {
            refreshingInputs = true;
            update();
            refreshingInputs = false;
        }

        if (didRemove) {
            for (NodeIOWidget i : getIOWidgets()) {
                if (!i.isInput()) continue;
                if (i.connections.isEmpty()) continue;

                double targetY = i.pos().y() - 1 / 8f;
                for (WireWidget w : i.connections) {
                    if (w.line.to.y() == targetY) continue;
                    if (w.line.from.y() != w.line.to.y()) {
                        w.line.to = w.line.to.withY(targetY);
                        w.line.update();
                    } else {
                        if (w.previousWires.isEmpty() || w.previousWires.getFirst().line.from.y() == w.previousWires.getFirst().line.to.y()) {
                            WidgetVec mid = w.line.from.add(w.line.to).div(2);
                            List<WireWidget> wires = w.splitWire(mid);
                            WireWidget nw = new WireWidget(wires.getFirst(), w.type(), mid);
                            nw.addNextWire(wires.getLast());
                            wires.getFirst().nextWires.remove(wires.getLast());
                            wires.getLast().addPreviousWire(nw);
                            wires.getLast().previousWires.remove(wires.getFirst());
                            wires.getLast().line.from = wires.getLast().line.from.withY(targetY);
                            wires.getLast().line.to = wires.getLast().line.to.withY(targetY);
                            nw.line.to = wires.getLast().line.from;
                            wires.getLast().update();
                            pos().editor().rootWidgets.add(nw);
                            nw.update();
                        } else {
                            w.previousWires.getFirst().line.to = w.previousWires.getFirst().line.to.withY(targetY);
                            w.previousWires.getFirst().update();
                            w.line.from = w.line.from.withY(targetY);
                            w.line.to = w.line.to.withY(targetY);
                            w.line.update();
                        }
                    }
                }
            }
        }
    }

    public void remove() {
        for (NodeIOWidget io : getIOWidgets()) {
            for (WireWidget wire : new ArrayList<>(io.connections)) {
                List<NodeIOWidget> inputs = wire.getInputs();
                List<NodeIOWidget> outputs = wire.getOutputs();
                wire.removeConnection();
                if (wire.type() == SignalType.INSTANCE && !outputs.getFirst().connections.isEmpty()) outputs.getFirst().connections.getFirst().cleanup();
                else if (!inputs.getFirst().connections.isEmpty()) inputs.getFirst().connections.getFirst().cleanup();
            }
        }
        root.remove();
        if (node instanceof FunctionCallNode call) {
            call.function.callNodes.remove(call);
        }
    }

    public void borderColor(TextColor color) {
        root.color(color);
    }

    public List<NodeIOWidget> getInputs() {
        List<NodeIOWidget> list = new ArrayList<>();
        collectIOWidgets(root, list);
        list = list.stream().filter(NodeIOWidget::isInput).toList();
        return list;
    }

    public List<NodeIOWidget> getOutputs() {
        List<NodeIOWidget> list = new ArrayList<>();
        collectIOWidgets(root, list);
        list = list.stream().filter(io -> !io.isInput()).toList();
        return list;
    }

    public List<NodeIOWidget> getIOWidgets() {
        List<NodeIOWidget> list = new ArrayList<>();
        collectIOWidgets(root, list);
        return list;
    }

    private void collectIOWidgets(Widget node, List<NodeIOWidget> list) {
        if (node == null) {
            return;
        }

        if (node instanceof NodeIOWidget) {
            list.add((NodeIOWidget) node);
            return;
        }

        if (node.getChildren() == null) return;
        for (Widget widget : node.getChildren()) {
            collectIOWidgets(widget, list);
        }
    }

    @Override
    public List<Widget> getChildren() {
        return List.of(root);
    }

    @Override
    public boolean interact(CodeInteraction i) {
        for (NodeIOWidget io : getIOWidgets()) {
            for (WireWidget wire : io.connections) {
                for (WireWidget part : wire.getFullWire()) {
                    if (part.interact(i)) return true;
                }
            }
        }
        if (!inBounds(i.pos())) return false;
        if (root.interact(i)) return true;
        boolean lockedWire = false;
        for (NodeIOWidget io : getIOWidgets()) {
            for (WireWidget wire : io.connections) {
                if (i.pos().editor().isLocked(wire) != null && !i.pos().editor().isLockedByPlayer(wire, i.player())) {
                    lockedWire = true;
                    break;
                }
            }
        }
        if (i.type() == CodeInteraction.Type.LEFT_CLICK) {
            if (lockedWire) {
                i.player().sendMessage(Text.literal("Node is currently in use by another player!").formatted(Formatting.RED));
            } else {
                if (node instanceof FunctionInputsNode || node instanceof FunctionOutputsNode) {
                    i.player().sendMessage(Text.literal("Use /function delete to delete a function!").formatted(Formatting.RED));
                    return true;
                }
                remove();
                i.pos().editor().rootWidgets.remove(this);
            }
            return true;
        }
        if (i.type() == CodeInteraction.Type.RIGHT_CLICK && i.pos().editor().lockWidget(this, i.player())) {
            if (!lockedWire) i.pos().editor().setAction(i.player(), new DragNodeAction(this, pos().sub(i.pos()), i.player()));
            else i.player().sendMessage(Text.literal("Node is currently in use by another player!").formatted(Formatting.RED));
            return true;
        }
        return false;
    }
}
