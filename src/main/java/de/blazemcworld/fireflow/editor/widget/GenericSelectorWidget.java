package de.blazemcworld.fireflow.editor.widget;

import de.blazemcworld.fireflow.editor.Bounds;
import de.blazemcworld.fireflow.editor.CodeEditor;
import de.blazemcworld.fireflow.editor.Widget;
import de.blazemcworld.fireflow.util.TextWidth;
import de.blazemcworld.fireflow.value.Value;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class GenericSelectorWidget implements Widget {

    private final List<ButtonWidget> buttons = new ArrayList<>();
    private final RectWidget border;
    private final TextWidget label;
    private final Bounds bounds;

    public GenericSelectorWidget(Vec origin, String name, List<Value> options, CodeEditor editor, Consumer<Value> callback) {
        double width = 0;
        for (Value option : options) {
            width = Math.max(width, TextWidth.calculate(option.getBaseName(), false));
        }
        width /= 40;

        double height = options.size() * 0.3;
        bounds = new Bounds(
            Vec.fromPoint(origin).add(-width * 0.5 - 0.1, 0.5 * height + 0.15, 0),
            Vec.fromPoint(origin).add(width * 0.5 + 0.1, -0.5 * height - 0.05, 0)
        );
        border = new RectWidget(editor.inst, bounds);

        Vec pos = Vec.fromPoint(origin).add(width * 0.5, 0.5 * height - 0.25, 0);
        label = new TextWidget(pos, editor.inst, Component.text("» Select " + name + ":", NamedTextColor.YELLOW, TextDecoration.ITALIC));

        for (Value option : options) {
            pos = Vec.fromPoint(pos).add(0, -0.3, 0);
            ButtonWidget btn = new ButtonWidget(pos, editor.inst, Component.text(option.getBaseName()).color(option.getColor()));
            btn.rightClick = (player, _editor) -> {
                if (option.possibleGenerics().isEmpty()) {
                    callback.accept(option);
                } else {
                    GenericSelectorWidget.choose(origin, editor, option.possibleGenerics(), (chosen) -> callback.accept(option.fromGenerics(chosen)));
                }
                editor.remove(this);
            };
            btn.leftClick = (player, _editor) -> editor.remove(this);
            buttons.add(btn);
        }
    }

    public static void choose(Vec pos, CodeEditor editor, List<Value.GenericParam> possible, Consumer<List<Value>> callback) {
        List<Value> out = new ArrayList<>();
        AtomicReference<Consumer<Value>> next = new AtomicReference<>();
        next.set((chosen) -> {
            out.add(chosen);
            if (out.size() >= possible.size()) {
                callback.accept(out);
                return;
            }
            Value.GenericParam nextPossible = possible.get(out.size());
            editor.widgets.add(new GenericSelectorWidget(pos, nextPossible.name(), nextPossible.possibleTypes(), editor, next.get()));
        });
        Value.GenericParam p = possible.getFirst();
        editor.widgets.add(new GenericSelectorWidget(pos, p.name(), p.possibleTypes(), editor, next.get()));
    }

    @Override
    public Widget select(Player player, Vec cursor) {
        for (ButtonWidget button : buttons) {
            Widget result = button.select(player, cursor);
            if (result != null) return result;
        }
        return bounds.includes2d(cursor) ? this : null;
    }

    @Override
    public void leftClick(Vec cursor, Player player, CodeEditor editor) {
        remove();
    }

    @Override
    public void remove() {
        label.remove();
        for (ButtonWidget button : buttons) {
            button.remove();
        }
        border.remove();
    }
}
