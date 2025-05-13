package de.blazemcworld.fireflow.code.widget;

import de.blazemcworld.fireflow.code.CodeEditor;
import de.blazemcworld.fireflow.code.CodeInteraction;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.NodeList;
import de.blazemcworld.fireflow.code.node.impl.function.FunctionCallNode;
import de.blazemcworld.fireflow.code.node.impl.function.FunctionDefinition;
import de.blazemcworld.fireflow.code.type.AllTypes;
import de.blazemcworld.fireflow.code.type.WireType;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

public class NodeMenuWidget extends Widget {

    private Widget root;
    public NodeIOWidget ioOrigin;

    public NodeMenuWidget(WidgetVec pos, NodeList.Category category, List<NodeList.Category> parents) {
        super(pos);
        VerticalContainerWidget menu = new VerticalContainerWidget(pos);

        if (parents != null && !parents.isEmpty()) {
            NodeList.Category parent = parents.get(parents.size() - 1);
            ButtonWidget button = new ButtonWidget(new TextWidget(pos, Text.literal("🠄 Back").formatted(Formatting.GRAY)));

            button.handler = interaction -> {
                if (interaction.type() != CodeInteraction.Type.RIGHT_CLICK) return false;
                remove();
                interaction.pos().editor().rootWidgets.remove(this);

                NodeMenuWidget n = new NodeMenuWidget(root.pos(), parent, parents.subList(0, parents.size() - 1));
                n.update();
                n.ioOrigin = ioOrigin;
                n.pos().editor().rootWidgets.add(n);
                return true;
            };

            menu.widgets.add(button);
        }

        GridWidget grid = new GridWidget(pos, 5);
        menu.widgets.add(grid);

        boolean emptyCategory = true;
        for (NodeList.Category subCategory : category.categories) {
            if (subCategory.isFunctions && subCategory.filter != null) {
                boolean hasEntry = false;
                for (FunctionDefinition fn : pos().editor().functions.values()) {
                    FunctionCallNode fnNode = new FunctionCallNode(fn);
                    fn.callNodes.remove(fnNode); // Remove since it's not actually a real node
                    if (subCategory.filter.test(fnNode)) {
                        hasEntry = true;
                        break;
                    }
                }
                if (!hasEntry) continue;
            }

            ButtonWidget button = new ButtonWidget(new IconWidget(pos, new ItemStack(subCategory.icon), subCategory.name, 0.652));

            button.handler = interaction -> {
                if (interaction.type() != CodeInteraction.Type.RIGHT_CLICK) return false;
                remove();
                interaction.pos().editor().rootWidgets.remove(this);

                List<NodeList.Category> path = new ArrayList<>();
                if (parents != null) path.addAll(parents);
                path.add(category);
                NodeMenuWidget n = new NodeMenuWidget(root.pos(), subCategory, path);
                n.update();
                n.ioOrigin = ioOrigin;
                interaction.pos().editor().rootWidgets.add(n);
                return true;
            };

            grid.widgets.add(button);
            emptyCategory = false;
        }

        List<Node> nodes = category.nodes;
        if (category.isFunctions) {
            nodes = new ArrayList<>();
            for (FunctionDefinition fn : pos().editor().functions.values()) {
                FunctionCallNode fnNode = new FunctionCallNode(fn);
                fn.callNodes.remove(fnNode); // Remove since its not actually a real node
                if (category.filter == null || category.filter.test(fnNode)) nodes.add(fnNode);
            }
        }

        for (Node node : nodes) {
            ButtonWidget button = new ButtonWidget(new IconWidget(pos, new ItemStack(node.icon), node.name, 0.652));

            button.handler = interaction -> {
                if (interaction.type() != CodeInteraction.Type.RIGHT_CLICK) return false;
                remove();
                interaction.pos().editor().rootWidgets.remove(this);

                createNode(interaction.pos(), node, new ArrayList<>(), ioOrigin);
                return true;
            };

            grid.widgets.add(button);
            emptyCategory = false;
        }

        if (emptyCategory) {
            menu.widgets.remove(grid);
            menu.widgets.add(new TextWidget(pos, Text.literal("Empty category")));
        }

        BorderWidget<VerticalContainerWidget> border = new BorderWidget<>(menu);
        border.backgroundColor(0xdd000011);
        ButtonWidget button = new ButtonWidget(border);

        button.handler = interaction -> {
            if (interaction.type() == CodeInteraction.Type.LEFT_CLICK) {
                remove();
                interaction.pos().editor().rootWidgets.remove(this);
                return true;
            }
            return false;
        };

        root = button;
    }

    public static void createNode(WidgetVec pos, Node node, List<WireType<?>> types, NodeIOWidget ioOrigin) {
        CodeEditor e = pos.editor();
        if (node instanceof FunctionCallNode call) {
            if (!e.functions.containsKey(call.function.name)) return;
            if (e.functions.get(call.function.name) != call.function) return;
        }
        if (node.getTypeCount() > types.size()) {
            List<WireType<?>> filtered = new ArrayList<>();
            for (WireType<?> type : AllTypes.all) {
                if (node.acceptsType(type, types.size())) {
                    filtered.add(type);
                }
            }

            TypeSelectorWidget selector = new TypeSelectorWidget(pos, filtered, type -> {
                types.add(type);
                createNode(pos, node.copyWithTypes(types), types, ioOrigin);
            });
            selector.pos(pos.add(selector.size().div(2)).gridAligned());
            selector.update();
            e.rootWidgets.add(selector);
            return;
        }

        NodeWidget n;
        if (types.isEmpty()) {
            n = new NodeWidget(pos, node.copy());
        } else {
            n = new NodeWidget(pos, node.copyWithTypes(types));
        }

        WidgetVec s = n.size();
        if (ioOrigin == null || !ioOrigin.parent.getIOWidgets().contains(ioOrigin) || !e.rootWidgets.contains(ioOrigin.parent)) {
            n.pos(pos.add(s.div(2)).gridAligned());
        } else {
            if (ioOrigin.isInput()) {
                n.pos(ioOrigin.pos().add(0.75 + s.x(), 0));
            } else {
                n.pos(ioOrigin.pos().sub(ioOrigin.size().x() + 0.75, 0));
            }

            n.update();
            Pair<Node.Input<?>, Node.Output<?>> compatible = getCompatible(n.node, ioOrigin);
            if (compatible != null) {
                if (compatible.first() != null && !ioOrigin.isInput()) {
                    for (NodeIOWidget io : n.getInputs()) {
                        if (io.input != compatible.first()) continue;
                        double yDiff = io.pos().y() - ioOrigin.pos().y();
                        n.pos(n.pos().sub(0, yDiff));
                        resolveOverlap(e, n);
                        n.update();
                        pair(io, ioOrigin, e);
                        break;
                    }
                } else if (compatible.second() != null && ioOrigin.isInput()) {
                    for (NodeIOWidget io : n.getOutputs()) {
                        if (io.output != compatible.second()) continue;
                        double yDiff = io.pos().y() - ioOrigin.pos().y();
                        n.pos(n.pos().sub(0, yDiff));
                        resolveOverlap(e, n);
                        n.update();
                        pair(ioOrigin, io, e);
                        break;
                    }
                }
            }

            for (NodeIOWidget io : n.getInputs()) {
                if (io.input.inset != null || io.input.connected != null) continue;
                List<NodeWidget> todo = new LinkedList<>();
                todo.add(ioOrigin.parent);
                Set<NodeWidget> checked = new HashSet<>();
                checked.add(n);

                connect:
                for (int i = 0; i < 16; i++) {
                    if (todo.isEmpty()) break;
                    NodeWidget w = todo.removeFirst();
                    checked.add(w);
                    for (NodeIOWidget other : w.getOutputs()) {
                        if (other.type() != io.type()) continue;
                        if (AllTypes.isValue(io.type()) || (other.connections.isEmpty() && io.connections.isEmpty())) {
                            pair(io, other, e);
                            break connect;
                        }
                    }

                    if (checked.size() + todo.size() >= 16) continue;

                    for (NodeIOWidget in : w.getIOWidgets()) {
                        for (WireWidget c : in.connections) {
                            for (NodeIOWidget io2 : c.getInputs()) {
                                if (checked.contains(io2.parent)) continue;
                                if (todo.contains(io2.parent)) continue;
                                todo.add(io2.parent);
                            }
                            for (NodeIOWidget io2 : c.getOutputs()) {
                                if (checked.contains(io2.parent)) continue;
                                if (todo.contains(io2.parent)) continue;
                                todo.add(io2.parent);
                            }
                        }
                    }
                }
            }
        }
        n.update();
        e.rootWidgets.add(n);
    }

    public static Pair<Node.Input<?>, Node.Output<?>> getCompatible(Node node, NodeIOWidget io) {
        Pair<Node.Input<?>, Node.Output<?>> compatible = null;
        if (io.isInput()) {
            for (Node.Output<?> out : node.outputs) {
                if (io.input.type == out.type) return Pair.of(null, out);
                if (isCompatible(io.input, out)) compatible = Pair.of(null, out);
            }
        } else {
            for (Node.Input<?> in : node.inputs) {
                if (io.output.type == in.type) return Pair.of(in, null);
                if (isCompatible(in, io.output)) compatible = Pair.of(in, null);
            }
        }
        return compatible;
    }

    private static void pair(NodeIOWidget input, NodeIOWidget output, CodeEditor editor) {
        WidgetVec start = output.pos().sub(output.size().sub(0, 1 / 8f));
        WidgetVec end = input.pos().sub(1 / 8f, 1 / 8f);

        List<WireWidget> wires = new ArrayList<>();
        List<WidgetVec> path = editor.pathfinder.findPath(start.sub(0.375, 0), end.add(0.375, 0));
        path.addFirst(start);
        path.add(end);

        for (int i = 0; i < path.size() - 1; i++) {
            wires.add(new WireWidget(path.get(i), output.type(), path.get(i + 1)));
        }

        for (int i = 1; i < wires.size(); i++) {
            wires.get(i - 1).connectNext(wires.get(i));
        }

        if (AllTypes.isValue(input.type())) {
            for (WireWidget w : new ArrayList<>(input.connections)) {
                w.removeConnection();
            }
        }
        if (!AllTypes.isValue(output.type())) {
            for (WireWidget w : new ArrayList<>(output.connections)) {
                w.removeConnection();
            }
        }

        wires.getFirst().connectPrevious(output);
        wires.getLast().connectNext(input);
        output.connect(wires.getFirst());
        input.connect(wires.getLast());

        for (WireWidget wire : wires) {
            editor.rootWidgets.add(wire);
            wire.update();
        }
        wires.getFirst().cleanup();
    }

    private static void resolveOverlap(CodeEditor editor, NodeWidget node) {
        WidgetVec pos = node.pos();
        WidgetVec size = node.size();
        Set<NodeWidget> relevant = new HashSet<>();
        for (Widget w : editor.rootWidgets) {
            if (!(w instanceof NodeWidget other)) continue;
            if (other == node) continue;
            WidgetVec otherPos = other.pos();
            WidgetVec otherSize = other.size();

            if (otherPos.y() - otherSize.y() - 0.25 > pos.y()) continue;
            if (otherPos.x() < pos.x() - size.x() || pos.x() < otherPos.x() - otherSize.x()) continue;

            relevant.add(other);
        }

        for (int i = 0; i < 16; i++) {
            boolean neededAdjustment = false;
            for (NodeWidget other : relevant) {
                WidgetVec otherPos = other.pos();
                WidgetVec otherSize = other.size();

                if (otherPos.y() - otherSize.y() - 0.25 > pos.y()) continue;
                if (pos.y() - size.y() - 0.25 > otherPos.y()) continue;

                neededAdjustment = true;
                pos = pos.withY(otherPos.y() - otherSize.y() - 0.5);
            }
            if (!neededAdjustment) break;
        }

        node.pos(pos);
    }

    private static boolean isCompatible(Node.Input<?> in, Node.Output<?> out) {
        return out.type == null || in.type == null || out.type == in.type || in.type.canConvert(out.type);
    }

    @Override
    public WidgetVec size() {
        return root.size();
    }

    @Override
    public void update() {
        root.pos(pos());
        root.update();
    }

    @Override
    public List<Widget> getChildren() {
        return List.of(root);
    }

    @Override
    public void remove() {
        root.remove();
    }

    @Override
    public boolean interact(CodeInteraction i) {
        return root.interact(i);
    }
}
