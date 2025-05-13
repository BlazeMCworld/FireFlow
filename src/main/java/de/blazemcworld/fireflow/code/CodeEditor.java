package de.blazemcworld.fireflow.code;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.code.action.CodeAction;
import de.blazemcworld.fireflow.code.action.DeleteSelectAction;
import de.blazemcworld.fireflow.code.action.SelectAction;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.NodeList;
import de.blazemcworld.fireflow.code.node.impl.function.FunctionCallNode;
import de.blazemcworld.fireflow.code.node.impl.function.FunctionDefinition;
import de.blazemcworld.fireflow.code.node.impl.function.FunctionInputsNode;
import de.blazemcworld.fireflow.code.node.impl.function.FunctionOutputsNode;
import de.blazemcworld.fireflow.code.type.AllTypes;
import de.blazemcworld.fireflow.code.widget.*;
import de.blazemcworld.fireflow.space.Space;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.InteractionEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CodeEditor {
    public final Space space;
    public final CodeWorld world;
    public final Set<Widget> rootWidgets = new HashSet<>();
    public final Pathfinder pathfinder = new Pathfinder(this);
    public final HashMap<ServerPlayerEntity, Set<Widget>> lockedWidgets = new HashMap<>();
    private final HashMap<ServerPlayerEntity, CodeAction> actions = new HashMap<>();
    public final HashMap<String, FunctionDefinition> functions = new HashMap<>();
    private final Path codePath;

    public CodeEditor(Space space, CodeWorld world) {
        this.space = space;
        this.world = world;
        codePath = space.path().resolve("code.json");
    }

    public void enterCode(ServerPlayerEntity player) {
        player.getAbilities().flying = true;
        player.getAbilities().allowFlying = true;
        player.sendAbilitiesUpdate();

        InteractionEntity helper = new InteractionEntity(EntityType.INTERACTION, world);
        helper.setInteractionHeight(-0.5f);
        helper.setInteractionWidth(-0.5f);
        helper.setPosition(Vec3d.ZERO);
        world.spawnEntity(helper);
        helper.vehicle = player;
        player.addPassenger(helper);
    }

    public void exitCode(ServerPlayerEntity player) {
        for (Entity helper : new ArrayList<>(player.getPassengerList())) {
            if (helper instanceof InteractionEntity) helper.remove(Entity.RemovalReason.DISCARDED);
        }

        if (actions.containsKey(player)) actions.get(player).stop(this, player);
        actions.remove(player);
        lockedWidgets.remove(player);
    }

    public boolean handleInteraction(ServerPlayerEntity player, CodeInteraction.Type type) {
        return handleInteraction(player, type, null);
    }

    public boolean handleInteraction(ServerPlayerEntity player, CodeInteraction.Type type, String message) {
        Optional<WidgetVec> cursor = getCodeCursor(player);
        if (cursor.isEmpty()) return false;
        CodeInteraction i = new CodeInteraction(player, cursor.get(), type, message);

        if (actions.containsKey(player)) {
            return actions.get(player).interact(i);
        }

        for (Widget w : new HashSet<>(rootWidgets)) {
            if (w.inBounds(i.pos()) && isLocked(w) != null && !isLockedByPlayer(w, player)) {
                player.sendMessage(Text.literal("Widget is currently in use by another player!").formatted(Formatting.RED));
                return true;
            }
            if (w.interact(i)) return true;
        }

        if (type == CodeInteraction.Type.RIGHT_CLICK) {
            actions.put(player, new SelectAction(i.pos()));
            return true;
        } else if (type == CodeInteraction.Type.SWAP_HANDS) {
            NodeMenuWidget n = new NodeMenuWidget(i.pos(), NodeList.root, null);
            n.pos(i.pos().add(n.size().div(2)));
            n.update();
            rootWidgets.add(n);
            return true;
        } else if (type == CodeInteraction.Type.LEFT_CLICK) {
            actions.put(player, new DeleteSelectAction(i.pos()));
            return true;
        }

        return false;
    }

    public Optional<WidgetVec> getCodeCursor(ServerPlayerEntity origin) {
        if (origin.getWorld() != world) return Optional.empty();
        if (origin.getRotationVec(0).getZ() <= 0.1) return Optional.empty();
        double scale = Math.abs(origin.getZ() - 16) / origin.getRotationVec(0).getZ();
        Vec3d pos = origin.getEyePos().add(origin.getRotationVec(0).multiply(scale));
        if (world.getBottomY() > pos.getY() || world.getTopYInclusive() + 1 < pos.getY()) return Optional.empty();
        if (pos.getX() > 256 || pos.getX() < -256) return Optional.empty();
        return Optional.of(new WidgetVec(this, pos.getX(), pos.getY()));
    }

    public void addNode(ServerPlayerEntity player, String query, boolean isSearch) {
        String lowerQuery = query.toLowerCase();
        Optional<WidgetVec> cursor = getCodeCursor(player);
        if (cursor.isEmpty()) return;

        NodeIOWidget ioOrigin = selectIOWidget(cursor.get());

        if (isSearch) {
            NodeMenuWidget n = new NodeMenuWidget(cursor.get(), NodeList.root.filtered((node) -> node.name.toLowerCase().contains(lowerQuery)), null);
            n.ioOrigin = ioOrigin;
            WidgetVec s = n.size();
            n.pos(cursor.get().add(s.div(2)).gridAligned());
            n.update();
            rootWidgets.add(n);
        } else {
            Node node = FuzzySearch.extractOne(query, NodeList.root.collectNodes(), n -> n.name.toLowerCase()).getReferent();
            NodeMenuWidget.createNode(cursor.get(), node, new ArrayList<>(), ioOrigin);
        }
    }


    public void unlockWidget(Widget widget, ServerPlayerEntity player) {
        lockedWidgets.computeIfAbsent(player, p -> new HashSet<>()).remove(widget);
    }

    public void unlockWidgets(List<Widget> widgets, ServerPlayerEntity player) {
        widgets.forEach(lockedWidgets.computeIfAbsent(player, p -> new HashSet<>())::remove);
    }

    public void unlockWidgets(ServerPlayerEntity player) {
        lockedWidgets.remove(player);
    }

    public List<Widget> lockWidgets(List<Widget> widgets, ServerPlayerEntity player) {
        List<Widget> failed = new ArrayList<>();
        for (Widget widget : widgets) {
            if (!lockWidget(widget, player)) failed.add(widget);
        }
        return failed;
    }

    public boolean lockWidget(Widget widget, ServerPlayerEntity player) {
        ServerPlayerEntity widgetLockedBy = isLocked(widget);
        if (widgetLockedBy != null) return widgetLockedBy == player;
        lockedWidgets.computeIfAbsent(player, p -> new HashSet<>()).add(widget);
        return true;
    }

    public ServerPlayerEntity isLocked(Widget widget) {
        for (Map.Entry<ServerPlayerEntity, Set<Widget>> entry : lockedWidgets.entrySet()) {
            if (entry.getValue().contains(widget)) return entry.getKey();
        }
        return null;
    }

    public boolean isLockedByPlayer(Widget widget, ServerPlayerEntity player) {
        return lockedWidgets.containsKey(player) && lockedWidgets.get(player).contains(widget);
    }

    public void setAction(ServerPlayerEntity player, CodeAction action) {
        if (actions.containsKey(player)) {
            actions.get(player).stop(this, player);
        }
        actions.put(player, action);
    }

    public void stopAction(ServerPlayerEntity player) {
        if (actions.containsKey(player)) {
            actions.get(player).stop(this, player);
        }
        actions.remove(player);
    }

    public void createFunction(ServerPlayerEntity player, String name) {
        if (functions.containsKey(name)) {
            player.sendMessage(Text.literal("Function " + name + " already exists").formatted(Formatting.RED));
            return;
        }

        FunctionDefinition function = new FunctionDefinition(name, Items.COMMAND_BLOCK);
        functions.put(name, function);

        Optional<WidgetVec> pos = getCodeCursor(player);
        if (pos.isEmpty()) {
            player.sendMessage(Text.literal("You must be looking at the code wall!").formatted(Formatting.RED));
            return;
        }

        NodeWidget inputs = new NodeWidget(pos.get(), function.inputsNode);
        NodeWidget outputs = new NodeWidget(pos.get(), function.outputsNode);

        inputs.pos(pos.get().add(inputs.size().x(), 0));
        inputs.update();
        rootWidgets.add(inputs);

        outputs.pos(pos.get().sub(outputs.size().x(), 0));
        outputs.update();
        rootWidgets.add(outputs);
    }

    private FunctionDefinition tryGetFunction(ServerPlayerEntity player) {
        Optional<WidgetVec> pos = getCodeCursor(player);
        if (pos.isEmpty()) {
            player.sendMessage(Text.literal("You must be looking at the code wall!").formatted(Formatting.RED));
            return null;
        }

        FunctionDefinition function = null;
        for (Widget w : new HashSet<>(rootWidgets)) {
            if (w instanceof NodeWidget nodeWidget && nodeWidget.inBounds(pos.get())) {
                if (nodeWidget.node instanceof FunctionInputsNode inputsNode) {
                    function = inputsNode.function;

                    for (Node.Output<?> output : function.outputsNode.outputs) {
                        if (output.connected == null) continue;
                        player.sendMessage(Text.literal("Function is currently in use!").formatted(Formatting.RED));
                        return null;
                    }
                } else if (nodeWidget.node instanceof FunctionOutputsNode outputsNode) {
                    function = outputsNode.function;

                    for (Node.Input<?> input : function.inputsNode.inputs) {
                        if (input.connected == null) continue;
                        player.sendMessage(Text.literal("Function is currently in use!").formatted(Formatting.RED));
                        return null;
                    }
                }
            }
        }

        if (function == null) {
            player.sendMessage(Text.literal("You must be looking at a function.").formatted(Formatting.RED));
            return null;
        }

        if (!function.callNodes.isEmpty()) {
            player.sendMessage(Text.literal("Function is currently in use!").formatted(Formatting.RED));
            return null;
        }

        return function;
    }

    private void refreshFunctionWidgets(FunctionDefinition oldFunction, FunctionDefinition newFunction) {
        for (Widget w : new HashSet<>(rootWidgets)) {
            if (w instanceof NodeWidget old) {
                if (old.node instanceof FunctionInputsNode inputsNode && inputsNode.function == oldFunction) {
                    old.remove();
                    rootWidgets.remove(old);

                    NodeWidget updated = new NodeWidget(old.pos(), newFunction.inputsNode);
                    updated.update();
                    rootWidgets.add(updated);
                } else if (old.node instanceof FunctionOutputsNode outputsNode && outputsNode.function == oldFunction) {
                    old.remove();
                    rootWidgets.remove(old);

                    NodeWidget updated = new NodeWidget(old.pos(), newFunction.outputsNode);
                    updated.update();
                    rootWidgets.add(updated);
                }
            }
        }
    }

    public void deleteFunction(ServerPlayerEntity player) {
        FunctionDefinition function = tryGetFunction(player);
        if (function == null) return;

        functions.remove(function.name);
        for (Widget w : new HashSet<>(rootWidgets)) {
            if (w instanceof NodeWidget nodeWidget) {
                if (nodeWidget.node instanceof FunctionInputsNode inputsNode && inputsNode.function == function) {
                    nodeWidget.remove();
                    rootWidgets.remove(nodeWidget);
                } else if (nodeWidget.node instanceof FunctionOutputsNode outputsNode && outputsNode.function == function) {
                    nodeWidget.remove();
                    rootWidgets.remove(nodeWidget);
                }
            }
        }
    }

    public void addFunctionInput(ServerPlayerEntity player, String name) {
        FunctionDefinition function = tryGetFunction(player);
        if (function == null) return;

        if (function.getInput(name) != null) {
            player.sendMessage(Text.literal("Input " + name + " already exists!").formatted(Formatting.RED));
            return;
        }

        Optional<WidgetVec> pos = getCodeCursor(player);
        if (pos.isEmpty()) {
            player.sendMessage(Text.literal("You must be looking at the code wall!").formatted(Formatting.RED));
            return;
        }

        TypeSelectorWidget typeSelectorWidget = new TypeSelectorWidget(pos.get(), List.copyOf(AllTypes.all), type -> {
            if (function.getInput(name) != null) return;

            function.addInput(name, type);
            refreshFunctionWidgets(function, function);
        });
        typeSelectorWidget.update();
        rootWidgets.add(typeSelectorWidget);
    }

    public void addFunctionOutput(ServerPlayerEntity player, String name) {
        FunctionDefinition function = tryGetFunction(player);
        if (function == null) return;

        if (function.getOutput(name) != null) {
            player.sendMessage(Text.literal("Output " + name + " already exists!").formatted(Formatting.RED));
            return;
        }

        Optional<WidgetVec> pos = getCodeCursor(player);
        if (pos.isEmpty()) {
            player.sendMessage(Text.literal("You must be looking at the code wall!").formatted(Formatting.RED));
            return;
        }

        TypeSelectorWidget typeSelectorWidget = new TypeSelectorWidget(pos.get(), List.copyOf(AllTypes.all), type -> {
            if (function.getOutput(name) != null) return;

            function.addOutput(name, type);
            refreshFunctionWidgets(function, function);
        });
        typeSelectorWidget.update();
        rootWidgets.add(typeSelectorWidget);
    }

    public void removeFunctionInput(ServerPlayerEntity player, String name) {
        FunctionDefinition function = tryGetFunction(player);
        if (function == null) return;

        if (function.getInput(name) == null) {
            player.sendMessage(Text.literal("Input " + name + " does not exist!").formatted(Formatting.RED));
            return;
        }

        FunctionDefinition adjusted = new FunctionDefinition(function.name, function.icon);
        for (Node.Output<?> input : function.inputsNode.outputs) {
            if (input.id.equals(name)) continue;
            adjusted.addInput(input.id, input.type);
        }
        for (Node.Input<?> output : function.outputsNode.inputs) {
            adjusted.addOutput(output.id, output.type);
        }
        functions.put(function.name, adjusted);
        refreshFunctionWidgets(function, adjusted);
    }

    public void removeFunctionOutput(ServerPlayerEntity player, String name) {
        FunctionDefinition function = tryGetFunction(player);
        if (function == null) return;

        if (function.getOutput(name) == null) {
            player.sendMessage(Text.literal("Output " + name + " does not exist!").formatted(Formatting.RED));
            return;
        }

        FunctionDefinition adjusted = new FunctionDefinition(function.name, function.icon);
        for (Node.Output<?> input : function.inputsNode.outputs) {
            adjusted.addInput(input.id, input.type);
        }
        for (Node.Input<?> output : function.outputsNode.inputs) {
            if (output.id.equals(name)) continue;
            adjusted.addOutput(output.id, output.type);
        }
        functions.put(function.name, adjusted);
        refreshFunctionWidgets(function, adjusted);
    }

    public void setFunctionIcon(ServerPlayerEntity player, String icon) {
        FunctionDefinition function = tryGetFunction(player);
        if (function == null) return;

        DataResult<Identifier> id = Identifier.validate(icon);
        Optional<Item> item = id.isSuccess() ? Registries.ITEM.getOptionalValue(id.getOrThrow()) : Optional.empty();

        if (item.isEmpty()) {
            player.sendMessage(Text.literal("Unknown item!").formatted(Formatting.RED));
            return;
        }

        FunctionDefinition adjusted = new FunctionDefinition(function.name, item.get());
        for (Node.Output<?> input : function.inputsNode.outputs) {
            adjusted.addInput(input.id, input.type);
        }
        for (Node.Input<?> output : function.outputsNode.inputs) {
            adjusted.addOutput(output.id, output.type);
        }
        functions.put(function.name, adjusted);
        refreshFunctionWidgets(function, adjusted);
    }

    public void createSnippet(ServerPlayerEntity player) {
        Set<NodeWidget> nodes = new HashSet<>();
        Set<WireWidget> wires = new HashSet<>();
        Set<FunctionDefinition> functions = new HashSet<>();
        Set<NodeWidget> todo = new HashSet<>();

        Optional<WidgetVec> pos = getCodeCursor(player);
        if (pos.isEmpty()) {
            player.sendMessage(Text.literal("You must be looking at the code wall!").formatted(Formatting.RED));
            return;
        }

        for (Widget w : rootWidgets) {
            if (!(w instanceof NodeWidget n)) continue;
            if (w.inBounds(pos.get())) {
                todo.add(n);
                nodes.add(n);
            }
        }

        while (!todo.isEmpty()) {
            NodeWidget n = todo.iterator().next();
            todo.remove(n);

            for (NodeIOWidget io : n.getIOWidgets()) {
                for (WireWidget wire : io.connections) {
                    wires.add(wire);
                    for (NodeIOWidget other : wire.getInputs()) {
                        if (nodes.contains(other.parent)) continue;
                        nodes.add(other.parent);
                        todo.add(other.parent);
                    }
                    for (NodeIOWidget other : wire.getOutputs()) {
                        if (nodes.contains(other.parent)) continue;
                        nodes.add(other.parent);
                        todo.add(other.parent);
                    }
                    wires.addAll(wire.getFullWire());
                }
            }

            if (n.node instanceof FunctionInputsNode f) {
                gatherSnippetFunction(f.function, nodes, todo);
                functions.add(f.function);
            }
            if (n.node instanceof FunctionOutputsNode f) {
                gatherSnippetFunction(f.function, nodes, todo);
                functions.add(f.function);
            }
            if (n.node instanceof FunctionCallNode f) {
                gatherSnippetFunction(f.function, nodes, todo);
                functions.add(f.function);
            }
        }

        List<NodeWidget> nodeList = new ArrayList<>(nodes);
        List<WireWidget> wireList = new ArrayList<>(wires);

        JsonObject json = new JsonObject();
        json.add("nodes", CodeJSON.nodeToJson(nodeList, v -> v.sub(pos.get())));
        json.add("wires", CodeJSON.wireToJson(wireList, nodeList::indexOf, v -> v.sub(pos.get())));
        json.add("functions", CodeJSON.fnToJson(functions));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            GZIPOutputStream gz = new GZIPOutputStream(out);
            gz.write(json.toString().getBytes(StandardCharsets.UTF_8));
            gz.finish();
        } catch (Exception e) {
            FireFlow.LOGGER.error("Error gzipping snippet!", e);
            player.sendMessage(Text.literal("Internal error!").formatted(Formatting.RED));
            return;
        }

        String data = new String(Base64.getEncoder().encode(out.toByteArray()));
        player.sendMessage(Text.literal("Snippet created! Click to copy.").setStyle(
                Style.EMPTY.withClickEvent(new ClickEvent.CopyToClipboard(data)).withColor(TextColor.fromFormatting(Formatting.AQUA))
        ));
    }

    public void placeSnippet(ServerPlayerEntity player, byte[] base64Str) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(base64Str));
            GZIPInputStream gz = new GZIPInputStream(in);
            JsonObject json = JsonParser.parseString(new String(gz.readNBytes(1048576))).getAsJsonObject();

            List<FunctionDefinition> definitions = CodeJSON.fnFromJson(json.getAsJsonArray("functions"));

            Set<String> newFunctionNames = new HashSet<>();
            for (FunctionDefinition fn : definitions) {
                if (functions.containsKey(fn.name)) {
                    player.sendMessage(Text.literal("Snippet contains function " + fn.name + " which already exists!").formatted(Formatting.RED));
                    return;
                }
                if (newFunctionNames.contains(fn.name)) {
                    player.sendMessage(Text.literal("Snippet contains multiple functions named " + fn.name + "!").formatted(Formatting.RED));
                    return;
                }
                newFunctionNames.add(fn.name);
            }

            Optional<WidgetVec> pos = getCodeCursor(player);
            if (pos.isEmpty()) {
                player.sendMessage(Text.literal("You must be looking at the code wall!").formatted(Formatting.RED));
                return;
            }
            WidgetVec cursor = pos.get().gridAligned();

            List<NodeWidget> nodeWidgets = CodeJSON.nodeFromJson(json.getAsJsonArray("nodes"), (id) -> {
                if (functions.containsKey(id)) {
                    return functions.get(id);
                } else {
                    for (FunctionDefinition definition : definitions) {
                        if (definition.name.equals(id)) return definition;
                    }
                }
                return null;
            }, this, v -> v.add(cursor));
            List<WireWidget> wireWidgets = CodeJSON.wireFromJson(this, json.getAsJsonArray("wires"), nodeWidgets::get, v -> v.add(cursor));

            if (nodeWidgets.contains(null) || wireWidgets.contains(null)) {
                player.sendMessage(Text.literal("Snippet only partially valid!").formatted(Formatting.RED));
                nodeWidgets.removeIf(Objects::isNull);
                wireWidgets.removeIf(Objects::isNull);
            }

            rootWidgets.addAll(nodeWidgets);
            rootWidgets.addAll(wireWidgets);
            for (NodeWidget n : nodeWidgets) n.update();
            for (WireWidget w : wireWidgets) w.update();
            for (FunctionDefinition fn : definitions) functions.put(fn.name, fn);

            player.sendMessage(Text.literal("Snippet placed!").formatted(Formatting.AQUA));
        } catch (Exception e) {
            FireFlow.LOGGER.warn("Error reading snippet!", e);
            player.sendMessage(Text.literal("Internal error!").formatted(Formatting.RED));
        }
    }

    private void gatherSnippetFunction(FunctionDefinition fn, Set<NodeWidget> all, Set<NodeWidget> todo) {
        for (Widget w : rootWidgets) {
            if (!(w instanceof NodeWidget other)) continue;
            if (other.node instanceof FunctionOutputsNode otherFn && otherFn.function == fn) {
                if (all.contains(other)) continue;
                all.add(other);
                todo.add(other);
            }
            if (other.node instanceof FunctionInputsNode otherFn && otherFn.function == fn) {
                if (all.contains(other)) continue;
                all.add(other);
                todo.add(other);
            }
            if (other.node instanceof FunctionCallNode otherFn && otherFn.function == fn) {
                if (all.contains(other)) continue;
                all.add(other);
                todo.add(other);
            }
        }
    }

    public List<Widget> getAllWidgetsBetween(CodeInteraction i, WidgetVec p1, WidgetVec p2) {
        List<NodeWidget> nodeWidgets = new ArrayList<>();
        for (Widget w : new HashSet<>(rootWidgets)) {
            if (w instanceof NodeWidget nodeWidget) {
                if (isVectorBetween(nodeWidget.pos(), p1, p2) && isVectorBetween(nodeWidget.pos().sub(nodeWidget.size()), p1, p2))
                    nodeWidgets.add(nodeWidget);
            }
        }

        List<Widget> widgets = new ArrayList<>(nodeWidgets);
        for (NodeWidget w : nodeWidgets) {
            for (NodeIOWidget io : w.getIOWidgets()) {
                for (WireWidget wire : io.connections) {
                    if (widgets.contains(wire)) continue;
                    List<NodeWidget> inputs = wire.getInputs().stream().map(widget -> widget.parent).toList();
                    List<NodeWidget> outputs = wire.getOutputs().stream().map(widget -> widget.parent).toList();
                    if (new HashSet<>(widgets).containsAll(inputs) && new HashSet<>(widgets).containsAll(outputs)) {
                        widgets.addAll(wire.getFullWire());
                    }
                }
            }
        }

        return widgets;
    }

    public static boolean isVectorBetween(WidgetVec v, WidgetVec p1, WidgetVec p2) {
        WidgetVec min = p1.min(p2);
        WidgetVec max = p1.max(p2);

        return min.x() < v.x() && min.y() < v.y()
                && max.x() > v.x() && max.y() > v.y();
    }

    public NodeIOWidget selectIOWidget(WidgetVec cursor) {
        for (Widget w : rootWidgets) {
            if (!(w instanceof NodeWidget n)) continue;
            for (NodeIOWidget i : n.getIOWidgets()) {
                if (i.inBounds(cursor)) return i;
            }
        }
        return null;
    }

    public void save() {
        JsonObject data = new JsonObject();

        List<NodeWidget> nodeWidgets = new ArrayList<>();
        for (Widget widget : rootWidgets) {
            if (widget instanceof NodeWidget nodeWidget) {
                nodeWidgets.add(nodeWidget);
            }
        }
        data.add("nodes", CodeJSON.nodeToJson(nodeWidgets, v -> v));

        List<WireWidget> wireWidgets = new ArrayList<>();
        for (Widget widget : rootWidgets) {
            if (widget instanceof WireWidget wireWidget) {
                if (!wireWidget.isValid()) continue;
                wireWidgets.add(wireWidget);
            }
        }
        data.add("wires", CodeJSON.wireToJson(wireWidgets, nodeWidgets::indexOf, v -> v));
        data.add("functions", CodeJSON.fnToJson(this.functions.values()));

        try {
            if (!Files.exists(codePath.getParent())) Files.createDirectories(codePath.getParent());
            Files.writeString(codePath, data.toString());
        } catch (IOException e) {
            FireFlow.LOGGER.error("Failed to save code.json for space " + space.info.id + "!", e);
        }
    }

    public void load() {
        try {
            if (!Files.exists(codePath)) return;
            JsonObject data = JsonParser.parseString(Files.readString(codePath)).getAsJsonObject();

            for (FunctionDefinition fn : CodeJSON.fnFromJson(data.getAsJsonArray("functions"))) {
                functions.put(fn.name, fn);
            }

            List<NodeWidget> nodeWidgets = CodeJSON.nodeFromJson(data.getAsJsonArray("nodes"), functions::get, this, v -> v);
            List<WireWidget> wireWidgets = CodeJSON.wireFromJson(this, data.getAsJsonArray("wires"), nodeWidgets::get, v -> v);

            if (nodeWidgets.contains(null) || wireWidgets.contains(null)) {
                nodeWidgets.removeIf(Objects::isNull);
                wireWidgets.removeIf(Objects::isNull);
            }

            rootWidgets.addAll(nodeWidgets);
            rootWidgets.addAll(wireWidgets);

            for (NodeWidget n : nodeWidgets) n.update();
            for (WireWidget w : wireWidgets) w.update();
        } catch (IOException e) {
            FireFlow.LOGGER.error("Failed to load code.json for space " + space.info.id + "!", e);
        }
    }

    public void tick() {
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (actions.containsKey(player)) {
                Optional<WidgetVec> cursor = getCodeCursor(player);
                if (cursor.isEmpty()) continue;
                actions.get(player).tick(cursor.get(), player);
            }
        }
    }
}
