package de.blazemcworld.fireflow.editor.widget;

import de.blazemcworld.fireflow.editor.Bounds;
import de.blazemcworld.fireflow.editor.CodeEditor;
import de.blazemcworld.fireflow.editor.Widget;
import de.blazemcworld.fireflow.util.Messages;
import de.blazemcworld.fireflow.util.TextWidth;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.instance.Instance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchWidget<T> implements Widget {
    private final List<ButtonWidget> buttons;
    private final TextWidget label;
    private final RectWidget border;
    private final Bounds bounds;

    public SearchWidget(Vec pos, Instance inst, String input, Collection<Entry<T>> list, Callback<T> callback) {
        int size = list.size();
        buttons = new ArrayList<>(size);
        double height = size * 0.3;
        double width = 80;

        List<Match<T>> matches = new ArrayList<>(size);
        Pattern regexp = Pattern.compile(Pattern.quote(input), Pattern.CASE_INSENSITIVE);
        for (Entry<T> entry : list) {
            Matcher match = regexp.matcher(entry.name);
            if (!match.find()) continue;
            width = Math.max(width, TextWidth.calculate(entry.name, false));
            matches.add(new Match<>(entry, match.start(), match.end()));
        }
        width /= 40;
        bounds = new Bounds(
                pos.add(-width / 2 - 0.1, height / 2 + 0.15, 0),
                pos.add(width / 2 + 0.1, height / 2 - 0.05, 0)
        );
        border = new RectWidget(inst, bounds);
        pos = pos.add(width / 2, height / 2 - 0.25, 0);
        label = new TextWidget(pos, inst, Component.text("» Results for \"" + input + "\"", NamedTextColor.YELLOW, TextDecoration.ITALIC));

        for (Match<T> m : matches) {
            Component text = Component.text().append(
                    Component.text(m.entry.name.substring(0, m.start == 0 ? 0 : m.start), NamedTextColor.WHITE),
                    Component.text(m.entry.name.substring(m.start, m.end), NamedTextColor.RED),
                    Component.text(m.entry.name.substring(m.end), NamedTextColor.WHITE)
            ).build();
            pos = pos.add(0, -0.3, 0);
            ButtonWidget b = new ButtonWidget(pos, inst, text);
            b.rightClick = (player, editor) -> {
                editor.remove(this);
                callback.accept(editor, m.entry);
            };
            b.leftClick = (player, editor) -> editor.remove(this);
            buttons.add(b);
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
        for (Widget w : buttons) w.remove();
        border.remove();
    }

    public static class SearchText<T> extends TextWidget {
        private static final Component text = Component.text("≡ Search", NamedTextColor.GRAY, TextDecoration.ITALIC);
        private static final Component msg = Messages.error("Type whilst looking to search!");
        private final Function<CodeEditor, Collection<Entry<T>>> supplier;
        private final Callback<T> callback;
        private final Vec resultsPos;
        public SearchText(Vec txtPos, Vec resultsPos, Instance inst, Function<CodeEditor, Collection<Entry<T>>> supplier, Callback<T> callback) {
            super(txtPos, inst, text);
            this.supplier = supplier;
            this.callback = callback;
            this.resultsPos = resultsPos;
        }

        @Override
        public void chat(Vec cursor, PlayerChatEvent event, CodeEditor editor) {
            editor.widgets.add(new SearchWidget<>(resultsPos, editor.inst, event.getMessage(), supplier.apply(editor), callback));
        }

        @Override
        public void leftClick(Vec cursor, Player player, CodeEditor editor) {
            player.sendMessage(msg);
        }

        @Override
        public void rightClick(Vec cursor, Player player, CodeEditor editor) {
            player.sendMessage(msg);
        }
    }

    public record Entry<T>(String name, Supplier<T> supplier) {}
    private record Match<T>(Entry<T> entry, int start, int end) {}
    @FunctionalInterface
    public interface Callback<T> {
        void accept(CodeEditor editor, Entry<T> entry);
    }
}
