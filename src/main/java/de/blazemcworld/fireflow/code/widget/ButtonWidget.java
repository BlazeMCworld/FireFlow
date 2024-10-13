package de.blazemcworld.fireflow.code.widget;

import de.blazemcworld.fireflow.code.Interaction;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.InstanceContainer;

import java.util.List;
import java.util.function.Function;

public class ButtonWidget implements Widget {

    private final Widget looks;
    public Function<Interaction, Boolean> handler = (i) -> false;

    public ButtonWidget(Widget looks) {
        this.looks = looks;
    }

    public ButtonWidget(Component text) {
        this(new TextWidget(text));
    }

    @Override
    public void setPos(Vec pos) {
        looks.setPos(pos);
    }

    @Override
    public Vec getPos() {
        return looks.getPos();
    }

    @Override
    public void update(InstanceContainer inst) {
        looks.update(inst);
    }

    @Override
    public void remove() {
        looks.remove();
    }

    @Override
    public Vec getSize() {
        return looks.getSize();
    }

    @Override
    public boolean interact(Interaction i) {
        if (!inBounds(i.pos())) return false;
        if (handler.apply(i)) return true;
        return looks.interact(i);
    }

    @Override
    public List<Widget> getChildren() {
        return List.of(looks);
    }
}
