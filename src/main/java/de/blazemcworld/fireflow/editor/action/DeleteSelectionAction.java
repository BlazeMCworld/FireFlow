package de.blazemcworld.fireflow.editor.action;

import de.blazemcworld.fireflow.editor.Bounds;
import de.blazemcworld.fireflow.editor.CodeEditor;
import de.blazemcworld.fireflow.editor.EditorAction;
import de.blazemcworld.fireflow.editor.widget.NodeWidget;
import de.blazemcworld.fireflow.editor.widget.RectWidget;
import de.blazemcworld.fireflow.inventory.DeleteInventory;
import de.blazemcworld.fireflow.preferences.PlayerIndex;
import de.blazemcworld.fireflow.preferences.Preference;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

import java.util.HashMap;
import java.util.Map;

public class DeleteSelectionAction implements EditorAction {

    private final RectWidget rect;
    private Vec start;
    private final CodeEditor editor;
    private final Player player;
    private final Map<NodeWidget, Vec> nodes = new HashMap<>();

    public DeleteSelectionAction(Instance inst, Vec start, Player player, CodeEditor editor) {
        this.player = player;
        this.editor = editor;
        this.rect = new RectWidget(inst, new Bounds(start, start));
        this.start = start;
        rect.color(NamedTextColor.RED);
    }

    private void callback(boolean delete) {
        if (delete) {
            for (NodeWidget node : nodes.keySet()) {
                node.remove();
                editor.remove(node);
            }
            nodes.clear();
            rect.remove();
        }
        editor.setAction(player, null);
    }

    @Override
    public void leftClick(Vec cursor) {
        for (NodeWidget node : editor.getNodesInBound(new Bounds(start, cursor))) {
            node.border.color(NamedTextColor.RED);
            nodes.put(node, node.origin.sub(cursor));
        }
        start = cursor;
        rect.remove();
        if (nodes.isEmpty()) {
            editor.setAction(player, null);
            return;
        }
        if (nodes.size() >= 5 && PlayerIndex.get(player).preferences.getOrDefault(Preference.DELETE, 0) == 0) {
            DeleteInventory.open(player, nodes.size(), this::callback);
        } else callback(true);
    }

    @Override
    public void rightClick(Vec cursor) {
        editor.setAction(player, null);
    }

    @Override
    public void tick(Vec cursor) {
        if (nodes.isEmpty()) rect.update(new Bounds(start, cursor));
    }

    @Override
    public void stop() {
        nodes.clear();
        rect.remove();
    }
}
