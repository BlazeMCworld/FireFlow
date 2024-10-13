package de.blazemcworld.fireflow.code.widget;

import de.blazemcworld.fireflow.code.Interaction;
import de.blazemcworld.fireflow.code.node.Node;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.InstanceContainer;

import java.util.List;
import java.util.Set;

public class NodeMenuWidget implements Widget {

    private final Widget root;

    public NodeMenuWidget(Set<Node> nodes) {
        VerticalContainerWidget list = new VerticalContainerWidget();

        for (Node node : nodes) {
            ButtonWidget button = new ButtonWidget(Component.text(node.getTitle()));

            button.handler = interaction -> {
                if (interaction.type() != Interaction.Type.RIGHT_CLICK) return false;
                remove();
                interaction.editor().rootWidgets.remove(this);

                NodeWidget n = new NodeWidget(node.copy());
                Vec s = n.getSize();
                n.setPos(interaction.pos().add(Math.round(s.x() * 4) / 8f, Math.round(s.y() * 4) / 8f, 0));
                n.update(interaction.editor().space.code);
                interaction.editor().rootWidgets.add(n);
                return true;
            };

            list.widgets.add(button);
        }

        ButtonWidget button = new ButtonWidget(new BorderWidget(list));

        button.handler = interaction -> {
            if (interaction.type() == Interaction.Type.LEFT_CLICK) {
                remove();
                interaction.editor().rootWidgets.remove(this);
                return true;
            }
            return false;
        };

        root = button;
    }

    @Override
    public void setPos(Vec pos) {
        root.setPos(pos);
    }

    @Override
    public Vec getPos() {
        return root.getPos();
    }

    @Override
    public Vec getSize() {
        return root.getSize();
    }

    @Override
    public void update(InstanceContainer inst) {
        root.update(inst);
    }

    @Override
    public void remove() {
        root.remove();
    }

    @Override
    public boolean interact(Interaction i) {
        return root.interact(i);
    }

    @Override
    public Widget getWidget(Vec pos) {
        if (!inBounds(pos)) return null;
        return root.getWidget(pos);
    }

    @Override
    public List<Widget> getChildren() {
        return List.of(root);
    }
}
