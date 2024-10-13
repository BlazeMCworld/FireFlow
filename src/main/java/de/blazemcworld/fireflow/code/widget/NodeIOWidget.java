package de.blazemcworld.fireflow.code.widget;

import de.blazemcworld.fireflow.code.Interaction;
import de.blazemcworld.fireflow.code.action.WireAction;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.WireType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.InstanceContainer;

import java.util.ArrayList;
import java.util.List;

public class NodeIOWidget implements Widget {

    private final TextWidget text;
    public final List<WireWidget> connections = new ArrayList<>();
    private final boolean isInput;
    private final WireType<?> type;
    private final Node.Output<?> output;
    private final Node.Input<?> input;

    public NodeIOWidget(Node.Output<?> output) {
        text = new TextWidget(Component.text(output.getName() + " ○").color(output.type.getColor()));
        type = output.type;
        isInput = false;
        this.output = output;
        this.input = null;
    }

    public NodeIOWidget(Node.Input<?> input) {
        text = new TextWidget(Component.text("○ " + input.getName()).color(input.type.getColor()));
        type = input.type;
        isInput = true;
        this.output = null;
        this.input = input;
    }

    @Override
    public void setPos(Vec pos) {
        text.setPos(pos);
    }

    @Override
    public Vec getPos() {
        return text.getPos();
    }

    @Override
    public Vec getSize() {
        return text.getSize();
    }

    @Override
    public void update(InstanceContainer inst) {
        text.update(inst);
    }

    @Override
    public void remove() {
        text.remove();
    }

    @Override
    public boolean interact(Interaction i) {
        if (!inBounds(i.pos())) return false;
        if (i.type() == Interaction.Type.RIGHT_CLICK) {
            WireWidget wire = new WireWidget(this, type, i.pos());
            connections.add(wire);
            i.editor().rootWidgets.add(wire);
            i.editor().setAction(i.player(), new WireAction(wire, getPos().sub(i.pos()), isInput));
            return true;
        }
        return false;
    }

    public boolean isInput() {
        return isInput;
    }

    public TextColor color() {
        return text.text().color();
    }

    public WireType<?> type() {
        return type;
    }

    public void connect(WireWidget wire) {

    }

    public void removed(WireWidget wire) {

    }

    @Override
    public List<Widget> getChildren() {
        return null;
    }
}
