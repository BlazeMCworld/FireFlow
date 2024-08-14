package de.blazemcworld.fireflow.editor.action;

import de.blazemcworld.fireflow.editor.Bounds;
import de.blazemcworld.fireflow.editor.CodeEditor;
import de.blazemcworld.fireflow.editor.EditorAction;
import de.blazemcworld.fireflow.editor.widget.NodeWidget;
import de.blazemcworld.fireflow.editor.widget.RectWidget;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

import java.util.HashMap;
import java.util.Map;

public class MoveSelectionAction implements EditorAction {

    private final RectWidget rect;
    private Vec start;
    private final CodeEditor editor;
    private final Player player;
    private final Map<NodeWidget, Vec> nodes = new HashMap<>();

    public MoveSelectionAction(Instance inst, Vec start, Player player, CodeEditor editor) {
        this.player = player;
        this.editor = editor;
        this.rect = new RectWidget(inst, new Bounds(start, start));
        this.start = start;
        rect.color(NamedTextColor.AQUA);
    }

    @Override
    public void rightClick(Vec cursor) {
        if (!nodes.isEmpty()) {
            editor.setAction(player, null);
        } else {
            for (NodeWidget node : editor.getNodesInBound(new Bounds(start, cursor))) {
                node.border.color(NamedTextColor.GREEN);
                nodes.put(node, node.origin.sub(cursor));
            }
            start = cursor;
            rect.remove();
            if (nodes.isEmpty()) editor.setAction(player, null);
        }
    }

    @Override
    public void leftClick(Vec cursor) {
        editor.setAction(player, null);
    }

    @Override
    public void tick(Vec cursor) {
        if (!nodes.isEmpty()) {
            for (NodeWidget node : nodes.keySet()) {
                node.origin = cursor.add(nodes.get(node));
                node.update(false);
            }
        } else rect.update(new Bounds(start, cursor));
    }

    @Override
    public void stop() {
        for (NodeWidget node : nodes.keySet()) {
            node.border.color(NamedTextColor.WHITE);
            node.update(false);
        }
        nodes.clear();
        rect.remove();
    }
}
