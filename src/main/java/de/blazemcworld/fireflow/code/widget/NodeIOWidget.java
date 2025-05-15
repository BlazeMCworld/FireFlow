package de.blazemcworld.fireflow.code.widget;

import de.blazemcworld.fireflow.code.CodeInteraction;
import de.blazemcworld.fireflow.code.action.WireAction;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.NodeList;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.WireType;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class NodeIOWidget extends Widget {

    private final TextWidget text;
    public final List<WireWidget> connections = new ArrayList<>();
    private final boolean isInput;
    private final WireType<?> type;
    public final Node.Output<?> output;
    public final Node.Input<?> input;
    public final NodeWidget parent;

    public NodeIOWidget(WidgetVec pos, NodeWidget parent, Node.Output<?> output) {
        super(pos);
        type = output.type;
        isInput = false;
        this.output = output;
        this.input = null;
        this.parent = parent;
        text = new TextWidget(pos);
        text.setText(displayText());
    }

    public NodeIOWidget(WidgetVec pos, NodeWidget parent, Node.Input<?> input) {
        super(pos);
        type = input.type;
        isInput = true;
        this.output = null;
        this.input = input;
        this.parent = parent;
        text = new TextWidget(pos);
        text.setText(displayText());
    }

    @Override
    public WidgetVec size() {
        return text.size();
    }

    @Override
    public void update() {
        text.pos(pos());
        text.setText(displayText());
        text.update();
    }

    @Override
    public void remove() {
        text.remove();
    }

    public boolean isInput() {
        return isInput;
    }

    public WireType<?> type() {
        return type;
    }

    @SuppressWarnings("unchecked")
    public void connect(WireWidget wire) {
        if (wire.type() == SignalType.INSTANCE) {
            NodeIOWidget input = wire.getOutputs().getFirst();
            for (NodeIOWidget output : wire.getInputs()) {
                ((Node.Output<Object>) output.output).connected = (Node.Input<Object>) input.input;
            }
        } else {
            NodeIOWidget output = wire.getInputs().getFirst();
            for (NodeIOWidget input : wire.getOutputs()) {
                ((Node.Input<Object>) input.input).connect((Node.Output<Object>) output.output);
            }
        }
        text.setText(displayText());
        parent.refreshInputs();
    }

    public void removed(WireWidget wire) {
        if (wire.type() == SignalType.INSTANCE) {
            if (wire.previousOutput != null) {
                wire.previousOutput.output.connected = null;
            }
        } else {
            if (wire.nextInput != null) {
                wire.nextInput.input.connect(null);
                if (wire.nextInput.input.varargsParent != null) wire.nextInput.input.varargsParent.update();
            }
        }
        text.setText(displayText());
        parent.refreshInputs();
    }

    public void insetValue(String value) {
        input.setInset(value);

        for (WireWidget w : new ArrayList<>(connections)) {
            List<NodeIOWidget> inputs = w.getInputs();
            List<NodeIOWidget> outputs = w.getOutputs();
            w.removeConnection();
            if (w.type() == SignalType.INSTANCE && !outputs.getFirst().connections.isEmpty())
                outputs.getFirst().connections.getFirst().cleanup();
            else if (!inputs.getFirst().connections.isEmpty()) inputs.getFirst().connections.getFirst().cleanup();
        }

        text.setText(displayText());
        parent.refreshInputs();
    }

    private Text displayText() {
        String str = isInput ? ((connections.isEmpty() ? "○ " : "⏺ ") + input.name) :
                (output.name + (connections.isEmpty() ? " ○" : " ⏺"));

        if (isInput && input.inset != null) {
            String insetText = input.inset;
            if (insetText.length() > 20) insetText = insetText.substring(0, 20) + "...";
            str = "⏹ " + insetText;
        }

        return Text.literal(str).setStyle(Style.EMPTY.withColor(type.color));
    }

    @Override
    public List<Widget> getChildren() {
        return List.of(text);
    }

    @Override
    public boolean interact(CodeInteraction i) {
        if (!inBounds(i.pos())) return false;
        if (isInput && i.type() == CodeInteraction.Type.CHAT) {
            if (i.pos().editor().isLocked(this) != null && !i.pos().editor().isLockedByPlayer(this, i.player())) {
                i.player().sendMessage(Text.literal("Node is currently in use by another player!").formatted(Formatting.RED));
                return true;
            }
            if (input.type.parseInset(i.message()) == null) {
                i.player().sendMessage(Text.literal("Failed to parse inset!").formatted(Formatting.RED));
                return true;
            }

            insetValue(i.message().replaceAll("(?<!\\\\)\\(space\\)", " "));
            update();
            return true;
        }
        if (isInput && input.options != null) {
            if (i.type() == CodeInteraction.Type.LEFT_CLICK && input.options.handleLeftClick(this::insetValue, this, i)) {
                return true;
            }
            if (i.type() == CodeInteraction.Type.RIGHT_CLICK && input.options.handleRightClick(this::insetValue, this, i)) {
                return true;
            }
        }
        if (i.type() == CodeInteraction.Type.RIGHT_CLICK) {
            if (!connections.isEmpty()) {
                if (isInput != (type == SignalType.INSTANCE)) {
                    return true;
                }
            }
            if (!this.isInput()) i.pos().editor().setAction(i.player(), new WireAction(this, i.pos().editor(), i.player()));
            return true;
        }
        if (i.type() == CodeInteraction.Type.LEFT_CLICK && isInput && input.inset != null) {
            insetValue(null);
            parent.update();
            return true;
        }
        if (i.type() == CodeInteraction.Type.SWAP_HANDS) {
            NodeMenuWidget menu = new NodeMenuWidget(i.pos(), NodeList.root.filtered(n -> NodeMenuWidget.getCompatible(n, this) != null), null);
            menu.ioOrigin = this;
            if (isInput) {
                menu.pos(pos().add(0.5 + menu.size().x(), 0.5));
            } else {
                menu.pos(pos().sub(size().x() + 0.5, -0.5));
            }
            menu.update();
            i.pos().editor().rootWidgets.add(menu);
            return true;
        }
        return false;
    }
}