package de.blazemcworld.fireflow.code.widget;

import de.blazemcworld.fireflow.code.CodeEditor;
import de.blazemcworld.fireflow.code.CodeInteraction;
import de.blazemcworld.fireflow.code.type.AllTypes;
import de.blazemcworld.fireflow.code.type.WireType;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TypeSelectorWidget extends Widget {

    private final BorderWidget<GridWidget> container;

    public TypeSelectorWidget(WidgetVec pos, List<WireType<?>> options, Consumer<WireType<?>> callback) {
        super(pos);
        container = new BorderWidget<>(new GridWidget(pos, 5));

        for (WireType<?> type : options) {
            ButtonWidget button = new ButtonWidget(new IconWidget(
                pos, new ItemStack(type.icon), type.getName(), 0.652
            ));
            button.handler = interaction -> {
                if (interaction.type() != CodeInteraction.Type.RIGHT_CLICK) return false;
                if (type.getTypeCount() == 0) {
                    callback.accept(type);
                } else {
                    selectSubtypes(type, pos(), interaction.pos().editor(), new ArrayList<>(), callback);
                }
                interaction.pos().editor().rootWidgets.remove(this);
                remove();
                return true;
            };
            container.inner.widgets.add(button);
        }
        container.backgroundColor(0xdd000011);
    }

    private static void selectSubtypes(WireType<?> type, WidgetVec pos, CodeEditor editor, List<WireType<?>> done, Consumer<WireType<?>> callback) {
        if (done.size() == type.getTypeCount()) {
            callback.accept(type.withTypes(done));
            return;
        }

        List<WireType<?>> filtered = new ArrayList<>();
        for (WireType<?> subtype : AllTypes.all) {
            if (type.acceptsType(subtype, done.size())) {
                filtered.add(subtype);
            }
        }

        TypeSelectorWidget selector = new TypeSelectorWidget(pos, filtered, subtype -> {
            done.add(subtype);
            selectSubtypes(type, pos, editor, done, callback);
        });
        selector.update();
        editor.rootWidgets.add(selector);
    }

    @Override
    public WidgetVec size() {
        return container.size();
    }

    @Override
    public List<Widget> getChildren() {
        return List.of(container);
    }

    @Override
    public void update() {
        container.pos(pos());
        container.update();
    }

    @Override
    public void remove() {
        container.remove();
    }


    @Override
    public boolean interact(CodeInteraction i) {
        if (container.interact(i)) return true;
        if (i.type() == CodeInteraction.Type.LEFT_CLICK) {
            remove();
            i.pos().editor().rootWidgets.remove(this);
            return true;
        }
        return false;
    }
}
