package de.blazemcworld.fireflow.editor.widget;

import de.blazemcworld.fireflow.editor.Bounds;
import de.blazemcworld.fireflow.editor.CodeEditor;
import de.blazemcworld.fireflow.editor.Widget;
import de.blazemcworld.fireflow.node.NodeCategory;
import de.blazemcworld.fireflow.util.TextWidth;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CategoryWidget implements Widget {
    private final List<ButtonWidget> buttons = new ArrayList<>();
    private final RectWidget border;
    private final Bounds bounds;
    private final TextWidget label;
    public Consumer<NodeWidget> selectCallback = null;

    public CategoryWidget(Vec pos, CodeEditor editor, NodeCategory category, Component text) {
        Vec originPos = pos;
        List<NodeCategory.Entry> entries = category.supplier().get(editor, pos);
        double height = entries.size() * 0.3;
        double width = 80;
        for (NodeCategory.Entry entry : entries) width = Math.max(width, TextWidth.calculate(entry.name(), false));
        width /= 40;
        bounds = new Bounds(
                pos.add(-width / 2 - 0.1, height / 2 + 0.15, 0),
                pos.add(width / 2 + 0.1, height / 2 - 0.05, 0)
        );
        border = new RectWidget(editor.inst, bounds);

        pos = pos.add(width / 2, height / 2 - 0.25, 0);
        label = new TextWidget(pos, editor.inst, text);

        for (NodeCategory.Entry entry : entries) {
            pos = pos.add(0, -0.3, 0);
            ButtonWidget btn = new ButtonWidget(pos, editor.inst, Component.text(entry.name()));
            btn.rightClick = (player, _e) -> {
                editor.remove(this);
                entry.callback().accept(node -> {
                    NodeWidget w = new NodeWidget(originPos, editor.inst, node);
                    editor.widgets.add(w);
                    if (selectCallback != null) selectCallback.accept(w);
                });
            };
            btn.leftClick = (player, _e) -> editor.remove(this);
            buttons.add(btn);
        }
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
        editor.remove(this);
    }

    @Override
    public void remove() {
        label.remove();
        for (Widget button : buttons) button.remove();
        border.remove();
    }
}
