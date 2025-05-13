package de.blazemcworld.fireflow.code.widget;

import de.blazemcworld.fireflow.code.CodeInteraction;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Function;

public class ButtonWidget extends Widget {

    private final Widget looks;
    public Function<CodeInteraction, Boolean> handler = (i) -> false;

    public ButtonWidget(Widget looks) {
        super(looks.pos());
        this.looks = looks;
    }

    public ButtonWidget(WidgetVec pos, Text text) {
        this(new TextWidget(pos, text));
    }

    @Override
    public void update() {
        looks.pos(pos());
        looks.update();
    }

    @Override
    public void remove() {
        looks.remove();
    }

    @Override
    public WidgetVec size() {
        return looks.size();
    }

    @Override
    public List<Widget> getChildren() {
        return List.of(looks);
    }

    @Override
    public boolean interact(CodeInteraction i) {
        if (!inBounds(i.pos())) return false;
        if (handler.apply(i)) return true;
        return looks.interact(i);
    }
}
