package de.blazemcworld.fireflow.code.action;

import de.blazemcworld.fireflow.code.CodeInteraction;
import de.blazemcworld.fireflow.code.node.impl.function.FunctionInputsNode;
import de.blazemcworld.fireflow.code.node.impl.function.FunctionOutputsNode;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.widget.*;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class DeleteSelectAction extends SelectAction {

    public DeleteSelectAction(WidgetVec pos) {
        super(pos);
        box.color(TextColor.fromFormatting(Formatting.RED));
    }

    @Override
    public boolean interact(CodeInteraction i) {
        if (i.type() == CodeInteraction.Type.RIGHT_CLICK) {
            i.pos().editor().stopAction(i.player());
            return true;
        }
        else if (i.type() == CodeInteraction.Type.LEFT_CLICK) {
            List<Widget> widgets = i.pos().editor().getAllWidgetsBetween(i, box.pos, i.pos());
            widgets = new ArrayList<>(widgets.stream().filter(widget -> !(widget instanceof NodeWidget) || (!(((NodeWidget) widget).node instanceof FunctionInputsNode) && !(((NodeWidget) widget).node instanceof FunctionOutputsNode))).toList());
            widgets.sort((w1, w2) -> {
                if (w1 instanceof WireWidget && w2 instanceof NodeWidget) return -1;
                if (w2 instanceof WireWidget && w1 instanceof NodeWidget) return 1;
                return 0;
            });
            for (Widget w : widgets) {
                if (w instanceof NodeWidget nodeWidget) {
                    for (NodeIOWidget io : nodeWidget.getIOWidgets()) {
                        for (WireWidget wire : new ArrayList<>(io.connections)) {
                            if (widgets.contains(wire)) continue;
                            List<NodeIOWidget> inputs = wire.getInputs();
                            List<NodeIOWidget> outputs = wire.getOutputs();
                            wire.removeConnection();
                            if (wire.type() == SignalType.INSTANCE && !outputs.getFirst().connections.isEmpty()) outputs.getFirst().connections.getFirst().cleanup();
                            else if (!inputs.getFirst().connections.isEmpty()) inputs.getFirst().connections.getFirst().cleanup();
                        }
                    }
                    nodeWidget.remove();
                    i.pos().editor().rootWidgets.remove(nodeWidget);
                }
            }
            i.pos().editor().stopAction(i.player());
            return true;
        }
        return false;
    }
}
