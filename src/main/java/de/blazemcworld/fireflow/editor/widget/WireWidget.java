package de.blazemcworld.fireflow.editor.widget;

import de.blazemcworld.fireflow.editor.CodeEditor;
import de.blazemcworld.fireflow.editor.EditorAction;
import de.blazemcworld.fireflow.editor.Widget;
import de.blazemcworld.fireflow.value.SignalValue;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

import java.util.ArrayList;
import java.util.List;

public class WireWidget implements Widget {

    public final NodeInputWidget input;
    public final NodeOutputWidget output;
    public final List<Vec> relays;
    private final List<LineWidget> lines = new ArrayList<>();

    public WireWidget(Instance inst, NodeInputWidget input, NodeOutputWidget output, List<Vec> relays) {
        this.input = input;
        this.output = output;
        this.relays = relays;

        LineWidget last = new LineWidget(inst);
        last.color = input.input.type.getColor();
        last.from = input.position.add(-0.1, 0.2, 0);
        for (Vec point : relays) {
            last.to = point;
            lines.add(last);
            last = new LineWidget(inst);
            last.color = input.input.type.getColor();
            last.from = point;
        }
        last.to = output.position.add(-output.bounds().size().x() + 0.05, 0.2, 0);
        lines.add(last);

        update();
    }

    public void update() {
        lines.getFirst().from = input.position.add(-0.1, 0.2, 0);
        lines.getLast().to = output.position.add(-output.bounds().size().x() + 0.05, 0.2, 0);
        for (LineWidget line : lines) {
            line.update();
        }
    }

    @Override
    public void remove() {
        for (LineWidget line : lines) {
            line.remove();
        }
    }

    @Override
    public Widget select(Player player, Vec cursor) {
        for (LineWidget line : lines) {
            if (line.distance(cursor) < 0.2) {
                return this;
            }
        }
        return null;
    }

    @Override
    public void leftClick(Vec cursor, Player player, CodeEditor editor) {
        int index = 0;
        for (Vec relay : relays) {
            if (relay.withZ(0).distance(cursor.withZ(0)) < 0.2) {
                relays.remove(index);
                LineWidget removed = lines.remove(index);
                removed.remove();
                lines.get(index).from = removed.from;
                lines.get(index).update();
                return;
            }
            index++;
        }
        if (input.input.type == SignalValue.INSTANCE) {
            output.disconnect();
        } else {
            input.disconnect();
        }
    }

    @Override
    public void rightClick(Vec cursor, Player player, CodeEditor editor) {
        int index = 0;
        for (Vec relay : relays) {
            if (relay.withZ(0).distance(cursor.withZ(0)) < 0.2) {
                dragRelay(index, player, editor);
                return;
            }
            index++;
        }
        index = 0;
        for (LineWidget line : lines) {
            if (line.distance(cursor) < 0.2) {
                relays.add(index, cursor);
                LineWidget created = new LineWidget(editor.inst);
                created.color = input.input.getType().getColor();
                created.from = line.from;
                created.to = cursor;
                line.from = cursor;
                lines.add(index, created);
                dragRelay(index, player, editor);
                return;
            }
            index++;
        }
    }

    private void dragRelay(int index, Player player, CodeEditor editor) {
        editor.setAction(player, new EditorAction() {
            @Override
            public void tick(Vec cursor) {
                relays.set(index, cursor);
                lines.get(index).to = cursor;
                lines.get(index).update();
                lines.get(index + 1).from = cursor;
                lines.get(index + 1).update();
            }

            @Override
            public void rightClick(Vec cursor) {
                editor.setAction(player, null);
            }
        });
    }
}
