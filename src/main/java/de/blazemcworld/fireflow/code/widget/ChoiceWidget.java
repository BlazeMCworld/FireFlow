package de.blazemcworld.fireflow.code.widget;

import de.blazemcworld.fireflow.code.CodeInteraction;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

public class ChoiceWidget extends Widget {

    private final BorderWidget<GridWidget> root;

    public ChoiceWidget(WidgetVec pos, List<String> choices, Consumer<String> callback) {
        super(pos);
        root = new BorderWidget<>(new GridWidget(pos, choices.size() / 30 + 1));

        for (String choice : choices) {
            ButtonWidget button = new ButtonWidget(new TextWidget(pos, Text.literal(choice)));
            button.handler = interaction -> {
                if (interaction.type() != CodeInteraction.Type.RIGHT_CLICK) return false;
                remove();
                callback.accept(choice);
                interaction.pos().editor().rootWidgets.remove(this);
                return true;
            };
            root.inner.widgets.add(button);
        }
        root.backgroundColor(0xdd000011);
    }

    @Override
    public void update() {
        root.pos(pos());
        root.update();
    }

    @Override
    public void remove() {
        root.remove();
    }

    @Override
    public WidgetVec size() {
        return root.size();
    }

    @Override
    public List<Widget> getChildren() {
        return List.of(root);
    }

    @Override
    public boolean interact(CodeInteraction i) {
        if (!inBounds(i.pos())) return false;
        if (i.type() == CodeInteraction.Type.LEFT_CLICK) {
            remove();
            i.pos().editor().rootWidgets.remove(this);
            return true;
        }
        return root.interact(i);
    }
}
