package de.blazemcworld.fireflow.code.action;

import de.blazemcworld.fireflow.code.Interaction;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.widget.NodeIOWidget;
import de.blazemcworld.fireflow.code.widget.NodeWidget;
import de.blazemcworld.fireflow.code.widget.Widget;
import de.blazemcworld.fireflow.code.widget.WireWidget;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Vec;

import java.util.ArrayList;
import java.util.List;

public class DeleteSelectAction extends SelectAction {

    public DeleteSelectAction(Vec pos) {
        super(pos);
        box.color(NamedTextColor.RED);
    }

    @Override
    public void interact(Interaction i) {
        if (i.type() == Interaction.Type.LEFT_CLICK) i.editor().stopAction(i.player());
        else if (i.type() == Interaction.Type.RIGHT_CLICK) {
            List<Widget> widgets = i.editor().getAllWidgetsBetween(i, box.pos, i.pos());
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
                            wire.removeConnection(i.editor());
                            if (wire.type() == SignalType.INSTANCE && !outputs.getFirst().connections.isEmpty()) outputs.getFirst().connections.getFirst().cleanup(i.editor());
                            else if (!inputs.getFirst().connections.isEmpty()) inputs.getFirst().connections.getFirst().cleanup(i.editor());
                        }
                    }
                }
                w.remove();
                i.editor().rootWidgets.remove(w);
            }
            i.editor().stopAction(i.player());
        }
    }
}
