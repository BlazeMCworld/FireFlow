package de.blazemcworld.fireflow.code.action;

import de.blazemcworld.fireflow.code.CodeEditor;
import de.blazemcworld.fireflow.code.Interaction;
import de.blazemcworld.fireflow.code.widget.NodeIOWidget;
import de.blazemcworld.fireflow.code.widget.NodeWidget;
import de.blazemcworld.fireflow.code.widget.WireWidget;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DragNodeAction implements Action {
    private final NodeWidget node;
    private final Vec offset;
    private final List<NodeIOWidget> iowidgets;

    public DragNodeAction(NodeWidget node, Vec offset, CodeEditor editor) {
        this.node = node;
        this.offset = offset;
        node.borderColor(NamedTextColor.AQUA);
        iowidgets = node.getIOWidgets();
        for (NodeIOWidget IOWidget : new ArrayList<>(iowidgets)) {
            for (WireWidget wire : new ArrayList<>(IOWidget.connections)) {
                List<WireWidget> prevWires;
                if (IOWidget.isInput()) prevWires = wire.previousWires;
                else prevWires = wire.nextWires;
                if (prevWires.size() != 1 || prevWires.getFirst().line.to.y() == prevWires.getFirst().line.from.y()) {
                    Vec mid = wire.line.from.add(wire.line.to).div(2);
                    List<WireWidget> splitWires = wire.splitWire(editor, mid);
                    WireWidget nw = new WireWidget(splitWires.getFirst(), wire.type(), mid);
                    nw.addNextWire(splitWires.getLast());
                    nw.setPos(mid);
                    splitWires.getFirst().addNextWire(nw);
                    splitWires.getFirst().nextWires.remove(splitWires.getLast());
                    splitWires.getLast().addPreviousWire(nw);
                    splitWires.getLast().previousWires.remove(splitWires.getFirst());
                    editor.rootWidgets.add(nw);
                    nw.update(editor.space.code);
                }
            }
        }
    }

    @Override
    public void tick(Vec cursor, CodeEditor editor, Player player) {
        node.setPos(cursor.add(offset));
        node.update(editor.space.code);
        for (NodeIOWidget IOWidget : iowidgets) {
            for (WireWidget wire : IOWidget.connections) {
                if (IOWidget.isInput()) {
                    wire.setPos(IOWidget.getPos().sub(1/8f-1/32f, 1/8f, 0));
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
    }

    @Override
    public void stop(CodeEditor editor, Player player) {
        node.borderColor(NamedTextColor.WHITE);
        editor.unlockWidget(node, player);
    }
}
