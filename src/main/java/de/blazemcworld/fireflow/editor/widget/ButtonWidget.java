package de.blazemcworld.fireflow.editor.widget;

import de.blazemcworld.fireflow.editor.CodeEditor;
import de.blazemcworld.fireflow.editor.Widget;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public class ButtonWidget extends TextWidget {

    public @Nullable BiConsumer<Player, CodeEditor> leftClick;
    public @Nullable BiConsumer<Player, CodeEditor> rightClick;

    public ButtonWidget(Vec position, Instance inst, Component text) {
        super(position, inst, text);
    }

    @Override
    public Widget select(Player player, Vec cursor) {
        return bounds().includes2d(cursor) ? this : null;
    }

    @Override
    public void leftClick(Vec cursor, Player player, CodeEditor editor) {
        if (leftClick != null) leftClick.accept(player, editor);
    }
    @Override
    public void rightClick(Vec cursor, Player player, CodeEditor editor) {
        if (rightClick != null) rightClick.accept(player, editor);
    }
}
