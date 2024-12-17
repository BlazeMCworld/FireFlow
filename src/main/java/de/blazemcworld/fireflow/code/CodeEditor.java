package de.blazemcworld.fireflow.code;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.code.action.Action;
import de.blazemcworld.fireflow.code.action.DeleteSelectAction;
import de.blazemcworld.fireflow.code.action.SelectAction;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.Node.Input;
import de.blazemcworld.fireflow.code.node.Node.Varargs;
import de.blazemcworld.fireflow.code.node.NodeList;
import de.blazemcworld.fireflow.code.node.impl.function.FunctionCallNode;
import de.blazemcworld.fireflow.code.node.impl.function.FunctionDefinition;
import de.blazemcworld.fireflow.code.node.impl.function.FunctionInputsNode;
import de.blazemcworld.fireflow.code.node.impl.function.FunctionOutputsNode;
import de.blazemcworld.fireflow.code.type.AllTypes;
import de.blazemcworld.fireflow.code.type.WireType;
import de.blazemcworld.fireflow.code.widget.*;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.util.PlayerExitInstanceEvent;
import de.blazemcworld.fireflow.util.Translations;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.InteractionMeta;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.item.Material;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CodeEditor {

    public final Space space;
    public final Set<Widget> rootWidgets = new HashSet<>();
    public final HashMap<Player, Set<Widget>> lockedWidgets = new HashMap<>();
    private final HashMap<Player, Action> actions = new HashMap<>();
    private final Path codePath;
    public final HashMap<String, FunctionDefinition> functions = new HashMap<>();
    public final Pathfinder pathfinder = new Pathfinder(this);

    public CodeEditor(Space space) {
        this.space = space;
        codePath = Path.of("spaces/" + space.info.id + "/code.json");

        EventNode<InstanceEvent> events = space.code.eventNode();

        events.addListener(PlayerSpawnEvent.class, event -> {
            Player player = event.getPlayer();

            player.setAllowFlying(true);
            player.setFlying(true);

            Entity interactionHelper = new Entity(EntityType.INTERACTION);
            interactionHelper.setNoGravity(true);
            InteractionMeta meta = (InteractionMeta) interactionHelper.getEntityMeta();
            meta.setWidth(-0.5f);
            meta.setHeight(-0.5f);
            interactionHelper.setInstance(event.getInstance(), Pos.ZERO);
            player.addPassenger(interactionHelper);

            lockedWidgets.put(player, new HashSet<>());
        });

        events.addListener(PlayerExitInstanceEvent.class, event -> {
            for (Entity passenger : event.getEntity().getPassengers()) {
                if (passenger.getEntityType() == EntityType.INTERACTION) passenger.remove();
            }

            if (actions.containsKey(event.getPlayer())) actions.get(event.getPlayer()).stop(this, event.getPlayer());
            actions.remove(event.getPlayer());
            lockedWidgets.remove(event.getPlayer());
        });

        events.addListener(PlayerEntityInteractEvent.class, event -> {
            handleInteraction(event.getPlayer(), Interaction.Type.RIGHT_CLICK);
        });
        events.addListener(PlayerSwapItemEvent.class, event -> {
            handleInteraction(event.getPlayer(), Interaction.Type.SWAP_HANDS);
        });
        events.addListener(EntityAttackEvent.class, event -> {
            if (event.getEntity() instanceof Player player) {
                handleInteraction(player, Interaction.Type.LEFT_CLICK);
            }
        });

        events.addListener(PlayerTickEvent.class, event -> {
            Action a = actions.get(event.getPlayer());
            if (a == null) return;
            Vec cursor = getCursor(event.getPlayer());
            a.tick(cursor, this, event.getPlayer());
        });

        events.addListener(PlayerChatEvent.class, event -> {
            Vec pos = getCursor(event.getPlayer()).mul(8).apply(Vec.Operator.CEIL).div(8).withZ(15.999);
            for (Widget w : rootWidgets) {
                if (w.getWidget(pos) instanceof NodeIOWidget input) {
                    if (!input.isInput()) return;
                    event.setCancelled(true);
                    if (input.type().parseInset(event.getRawMessage()) == null) {
                        event.getPlayer().sendMessage(Component.text(Translations.get("error.invalid.inset")).color(NamedTextColor.RED));
                        return;
                    }

                    input.insetValue(event.getRawMessage(), this);
                    input.update(space.code);
                    w.update(space.code);
                    return;
                }
            }
        });

        load();
    }

    private void handleInteraction(Player player, Interaction.Type type) {
        Vec pos = getCursor(player).mul(8).apply(Vec.Operator.CEIL).div(8).withZ(15.999);
        Interaction i = new Interaction(this, player, pos, type);

        if (actions.containsKey(player)) {
            actions.get(player).interact(i);
            return;
        }

        for (Widget w : new HashSet<>(rootWidgets)) {
            if (w.interact(i)) return;
        }

        if (type == Interaction.Type.RIGHT_CLICK) {
            actions.put(player, new SelectAction(pos));
        } else if (type == Interaction.Type.SWAP_HANDS) {
            NodeMenuWidget n = new NodeMenuWidget(NodeList.root, this, null);
            Vec s = n.getSize();
            n.setPos(pos.add(Math.round(s.x() * 4) / 8f, Math.round(s.y() * 4) / 8f, 0));
            n.update(space.code);
            rootWidgets.add(n);
        } else if (type == Interaction.Type.LEFT_CLICK) {
            actions.put(player, new DeleteSelectAction(pos));
        }
    }

    private Vec getCursor(Player player) {
        double norm = player.getPosition().direction().dot(new Vec(0, 0, -1));
        if (norm >= 0) return Vec.ZERO.withZ(15.999);
        Vec start = player.getPosition().asVec().add(0.0, player.getEyeHeight(), -16);
        double dist = -start.dot(new Vec(0, 0, -1)) / norm;
        if (dist < 0) return Vec.ZERO.withZ(15.999);
        Vec out = start.add(player.getPosition().direction().mul(dist)).withZ(15.999);
        if (out.y() > 99999) out = out.withY(99999);
        if (out.y() < -99999) out = out.withY(-99999);
        if (out.x() > 99999) out = out.withX(99999);
        if (out.x() < -99999) out = out.withX(-99999);
        return out;
    }

    public void unlockWidget(Widget widget, Player player) {
        lockedWidgets.computeIfAbsent(player, p -> new HashSet<>()).remove(widget);
    }

    public void unlockWidgets(Player player) {
        lockedWidgets.remove(player);
    }

    public boolean lockWidget(Widget widget, Player player) {
        for (Map.Entry<Player, Set<Widget>> entry : lockedWidgets.entrySet()) {
            if (entry.getValue().contains(widget)) return entry.getKey() == player;
        }
        lockedWidgets.computeIfAbsent(player, p -> new HashSet<>()).add(widget);
        return true;
    }

    public void setAction(Player player, Action action) {
        if (actions.containsKey(player)) {
            actions.get(player).stop(this, player);
        }
        actions.put(player, action);
    }

    public void stopAction(Player player) {
        if (actions.containsKey(player)) {
            actions.get(player).stop(this, player);
        }
        actions.remove(player);
    }

    public void createFunction(Player player, String name) {
        if (functions.containsKey(name)) {
            player.sendMessage(Component.text(Translations.get("error.function.exists")).color(NamedTextColor.RED));
            return;
        }

        FunctionDefinition function = new FunctionDefinition(name, Material.COMMAND_BLOCK);
        functions.put(name, function);

        Vec pos = getCursor(player).mul(8).apply(Vec.Operator.CEIL).div(8).withZ(15.999);

        NodeWidget inputs = new NodeWidget(function.inputsNode, this);
        NodeWidget outputs = new NodeWidget(function.outputsNode, this);

        inputs.setPos(pos.add(inputs.getSize().x(), 0, 0));
        inputs.update(space.code);
        rootWidgets.add(inputs);
        
        outputs.setPos(pos.sub(outputs.getSize().x(), 0, 0));
        outputs.update(space.code);
        rootWidgets.add(outputs);
    }

    private FunctionDefinition tryGetFunction(Player player) {
        Vec pos = getCursor(player);
        FunctionDefinition function = null;
        for (Widget w : new HashSet<>(rootWidgets)) {
            if (w instanceof NodeWidget nodeWidget && nodeWidget.inBounds(pos)) {
                if (nodeWidget.node instanceof FunctionInputsNode inputsNode) {
                    function = inputsNode.function;
                } else if (nodeWidget.node instanceof FunctionOutputsNode outputsNode) {
                    function = outputsNode.function;
                }
            }
        }

        if (function == null) {
            player.sendMessage(Component.text(Translations.get("error.needs.function")).color(NamedTextColor.RED));
            return null;
        }

        if (!function.callNodes.isEmpty()) {
            player.sendMessage(Component.text(Translations.get("error.function.in_use")).color(NamedTextColor.RED));
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

                    NodeWidget updated = new NodeWidget(newFunction.inputsNode, this);
                    updated.setPos(old.getPos());
                    updated.update(space.code);
                    rootWidgets.add(updated);
                } else if (old.node instanceof FunctionOutputsNode outputsNode && outputsNode.function == oldFunction) {
                    old.remove();
                    rootWidgets.remove(old);

                    NodeWidget updated = new NodeWidget(newFunction.outputsNode, this);
                    updated.setPos(old.getPos());
                    updated.update(space.code);
                    rootWidgets.add(updated);
                }
            }
        }
    }

    public void deleteFunction(Player player) {
        FunctionDefinition function = tryGetFunction(player);
        if (function == null) return;

        functions.remove(function.name);
        for (Widget w : new HashSet<>(rootWidgets)) {
            if (w instanceof NodeWidget nodeWidget) {
                if (nodeWidget.node instanceof FunctionInputsNode inputsNode && inputsNode.function == function) {
                    nodeWidget.remove(this);
                    rootWidgets.remove(nodeWidget);
                } else if (nodeWidget.node instanceof FunctionOutputsNode outputsNode && outputsNode.function == function) {
                    nodeWidget.remove(this);
                    rootWidgets.remove(nodeWidget);
                }
            }
        }
    }

    public void addFunctionInput(Player player, String name) {
        FunctionDefinition function = tryGetFunction(player);
        if (function == null) return;

        if (function.getInput(name) != null) {
            player.sendMessage(Component.text(Translations.get("error.input.exists")).color(NamedTextColor.RED));
            return;
        }

        TypeSelectorWidget typeSelectorWidget = new TypeSelectorWidget(List.copyOf(AllTypes.all), type -> {
            if (function.getInput(name) != null) return;

            function.addInput(name, type);
            refreshFunctionWidgets(function, function);
        });
        typeSelectorWidget.setPos(getCursor(player));
        typeSelectorWidget.update(space.code);
        rootWidgets.add(typeSelectorWidget);
    }

    public void addFunctionOutput(Player player, String name) {
        FunctionDefinition function = tryGetFunction(player);
        if (function == null) return;

        if (function.getOutput(name) != null) {
            player.sendMessage(Component.text(Translations.get("error.output.exists")).color(NamedTextColor.RED));
            return;
        }

        TypeSelectorWidget typeSelectorWidget = new TypeSelectorWidget(List.copyOf(AllTypes.all), type -> {
            if (function.getOutput(name) != null) return;

            function.addOutput(name, type);
            refreshFunctionWidgets(function, function);
        });
        typeSelectorWidget.setPos(getCursor(player));
        typeSelectorWidget.update(space.code);
        rootWidgets.add(typeSelectorWidget);
    }

    public void removeFunctionInput(Player player, String name) {
        FunctionDefinition function = tryGetFunction(player);
        if (function == null) return;

        if (function.getInput(name) == null) {
            player.sendMessage(Component.text(Translations.get("error.input.not_found")).color(NamedTextColor.RED));
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

    public void removeFunctionOutput(Player player, String name) {
        FunctionDefinition function = tryGetFunction(player);
        if (function == null) return;

        if (function.getOutput(name) == null) {
            player.sendMessage(Component.text(Translations.get("error.output.not_found")).color(NamedTextColor.RED));
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

    public void setFunctionIcon(Player player, String icon) {
        FunctionDefinition function = tryGetFunction(player);
        if (function == null) return;

        Material m = Material.fromNamespaceId(icon);
        
        if (m == null) {
            player.sendMessage(Component.text(Translations.get("error.unknown.item")).color(NamedTextColor.RED));
            return;
        }
        
        FunctionDefinition adjusted = new FunctionDefinition(function.name, m);
        for (Node.Output<?> input : function.inputsNode.outputs) {
            adjusted.addInput(input.id, input.type);
        }
        for (Node.Input<?> output : function.outputsNode.inputs) {
            adjusted.addOutput(output.id, output.type);
        }
        functions.put(function.name, adjusted);
        refreshFunctionWidgets(function, adjusted);
    }

    public List<Widget> getAllWidgetsBetween(Interaction i, Vec p1, Vec p2) {
        List<NodeWidget> nodeWidgets = new ArrayList<>();
        for (Widget w : new HashSet<>(i.editor().rootWidgets)) {
            if (w instanceof NodeWidget nodeWidget) {
                if (isVectorBetween(nodeWidget.getPos(), p1, p2) && isVectorBetween(nodeWidget.getPos().sub(nodeWidget.getSize()), p1, p2))
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

    private static boolean isVectorBetween(Vec v, Vec p1, Vec p2) {
        Vec min = p1.min(p2);
        Vec max = p1.max(p2);

        return min.x() < v.x() && min.y() < v.y()
                && max.x() > v.x() && max.y() > v.y();
    }

    public void save() {
        JsonObject data = new JsonObject();
        JsonArray nodes = new JsonArray();
        JsonArray wires = new JsonArray();
        
        List<NodeWidget> nodeWidgets = new ArrayList<>();
        for (Widget widget : rootWidgets) {
            if (widget instanceof NodeWidget nodeWidget) {
                nodeWidgets.add(nodeWidget);
            }
        }
        for (NodeWidget nodeWidget : nodeWidgets) {
            JsonObject entry = new JsonObject();
            entry.addProperty("type", nodeWidget.node.id);
            entry.addProperty("x", nodeWidget.getPos().x());
            entry.addProperty("y", nodeWidget.getPos().y());
            
            if (nodeWidget.node.getTypeCount() > 0) {
                JsonArray types = new JsonArray();
                for (WireType<?> type : nodeWidget.node.getTypes()) {
                    types.add(AllTypes.toJson(type));
                }
                entry.add("types", types);
            }

            JsonObject insets = new JsonObject();
            for (NodeIOWidget io : nodeWidget.getIOWidgets()) {
                if (io.isInput() && io.input.inset != null) {
                    insets.addProperty(io.input.id, io.input.inset);
                }
            }
            if (!insets.isEmpty()) {
                entry.add("insets", insets);
            }

            JsonObject inputs = new JsonObject();
            JsonObject outputs = new JsonObject();
            for (NodeIOWidget io : nodeWidget.getIOWidgets()) {
                if (io.isInput() && io.input.connected != null) {
                    int nodeIndex = 0;
                    for (NodeWidget node : nodeWidgets) {
                        if (node.node != io.input.connected.getNode()) {
                            nodeIndex++;
                            continue;
                        }
                        int outputIndex = node.node.outputs.indexOf(io.input.connected);
                        inputs.addProperty(io.input.id, nodeIndex + ":" + outputIndex);
                        break;
                    }
                } else if (!io.isInput() && io.output.connected != null) {
                    int nodeIndex = 0;
                    for (NodeWidget node : nodeWidgets) {
                        if (node.node != io.output.connected.getNode()) {
                            nodeIndex++;
                            continue;
                        }
                        int inputIndex = node.node.inputs.indexOf(io.output.connected);
                        outputs.addProperty(io.output.id, nodeIndex + ":" + inputIndex);
                        break;
                    }
                }
            }
            if (!inputs.isEmpty()) {
                entry.add("inputs", inputs);
            }
            if (!outputs.isEmpty()) {
                entry.add("outputs", outputs);
            }

            if (!nodeWidget.node.varargs.isEmpty()) {
                JsonObject varargsObj = new JsonObject();
                for (Varargs<?> varargs : nodeWidget.node.varargs) {
                    JsonArray varargsEntry = new JsonArray();
                    for (Input<?> input : varargs.children) {
                        if (input.inset == null && input.connected == null) continue;
                        varargsEntry.add(input.id);
                    }
                    varargsObj.add(varargs.id, varargsEntry);
                }
                entry.add("varargs", varargsObj);
            }

            if (nodeWidget.node instanceof FunctionInputsNode inputsNode) {
                entry.addProperty("function", inputsNode.function.name);
            } else if (nodeWidget.node instanceof FunctionOutputsNode outputsNode) {
                entry.addProperty("function", outputsNode.function.name);
            } else if (nodeWidget.node instanceof FunctionCallNode callNode) {
                entry.addProperty("function", callNode.function.name);
            }

            nodes.add(entry);
        }
        
        List<WireWidget> wireWidgets = new ArrayList<>();
        for (Widget widget : rootWidgets) {
            if (widget instanceof WireWidget wireWidget) {
                if (!wireWidget.isValid()) continue;
                wireWidgets.add(wireWidget);
            }
        }

        for (WireWidget wireWidget : wireWidgets) {
            JsonObject wireObj = new JsonObject();
            wireObj.add("type", AllTypes.toJson(wireWidget.type()));
            wireObj.addProperty("fromX", wireWidget.line.from.x());
            wireObj.addProperty("fromY", wireWidget.line.from.y());
            wireObj.addProperty("toX", wireWidget.line.to.x());
            wireObj.addProperty("toY", wireWidget.line.to.y());
            
            if (wireWidget.previousOutput != null) {
                wireObj.addProperty("previousOutputNode", nodeWidgets.indexOf(wireWidget.previousOutput.parent));
                wireObj.addProperty("previousOutputId", wireWidget.previousOutput.output.id);
            }

            JsonArray previousWires = new JsonArray();
            for (WireWidget previousWire : wireWidget.previousWires) {
                previousWires.add(wireWidgets.indexOf(previousWire));
            }
            if (!previousWires.isEmpty()) {
                wireObj.add("previousWires", previousWires);
            }
            
            if (wireWidget.nextInput != null) {
                wireObj.addProperty("nextInputNode", nodeWidgets.indexOf(wireWidget.nextInput.parent));
                wireObj.addProperty("nextInputId", wireWidget.nextInput.input.id);
            }

            JsonArray nextWires = new JsonArray();
            for (WireWidget nextWire : wireWidget.nextWires) {
                nextWires.add(wireWidgets.indexOf(nextWire));
            }
            if (!nextWires.isEmpty()) {
                wireObj.add("nextWires", nextWires);
            }
            
            wires.add(wireObj);
        }
        
        data.add("nodes", nodes);
        data.add("wires", wires);

        JsonArray functions = new JsonArray();
        for (FunctionDefinition function : this.functions.values()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", function.name);
            obj.addProperty("icon", function.icon.namespace().asString());
            JsonArray inputs = new JsonArray();
            for (Node.Output<?> input : function.inputsNode.outputs) {
                JsonObject inputObj = new JsonObject();
                inputObj.addProperty("name", input.id);
                inputObj.add("type", AllTypes.toJson(input.type));
                inputs.add(inputObj);
            }
            obj.add("inputs", inputs);
            JsonArray outputs = new JsonArray();
            for (Node.Input<?> output : function.outputsNode.inputs) {
                JsonObject outputObj = new JsonObject();
                outputObj.addProperty("name", output.id);
                outputObj.add("type", AllTypes.toJson(output.type));
                outputs.add(outputObj);
            }
            obj.add("outputs", outputs);
            functions.add(obj);
        }
        data.add("functions", functions);

        try {
            if (!Files.exists(codePath.getParent())) Files.createDirectories(codePath.getParent());
            Files.writeString(codePath, data.toString());
        } catch (IOException e) {
            FireFlow.LOGGER.error("Failed to save code.json for space " + space.info.id + "!", e);
        }
    }

    @SuppressWarnings("unchecked")
    public void load() {
        try {
            if (!Files.exists(codePath)) return;
            JsonObject data = JsonParser.parseString(Files.readString(codePath)).getAsJsonObject();
            
            JsonArray functions = data.getAsJsonArray("functions");
            for (JsonElement function : functions) {
                JsonObject obj = function.getAsJsonObject();
                String name = obj.get("name").getAsString();
                Material icon = obj.has("icon") ? Material.fromNamespaceId(obj.get("icon").getAsString()) : null;
                if (icon == null) icon = Material.COMMAND_BLOCK;
                FunctionDefinition functionDefinition = new FunctionDefinition(name, icon);

                for (JsonElement input : obj.getAsJsonArray("inputs")) {
                    JsonObject inputObj = input.getAsJsonObject();
                    String inputName = inputObj.get("name").getAsString();
                    functionDefinition.addInput(inputName, AllTypes.fromJson(inputObj.get("type")));
                }
                for (JsonElement output : obj.getAsJsonArray("outputs")) {
                    JsonObject outputObj = output.getAsJsonObject();
                    String outputName = outputObj.get("name").getAsString();
                    functionDefinition.addOutput(outputName, AllTypes.fromJson(outputObj.get("type")));
                }
                this.functions.put(name, functionDefinition);
            }

            JsonArray nodes = data.getAsJsonArray("nodes");
            List<NodeWidget> nodeWidgets = new ArrayList<>();
            
            Set<Runnable> todo = new HashSet<>();

            for (JsonElement nodeElem : nodes) {
                JsonObject nodeObj = nodeElem.getAsJsonObject();
                String type = nodeObj.get("type").getAsString();
                double x = nodeObj.get("x").getAsDouble();
                double y = nodeObj.get("y").getAsDouble();

                List<WireType<?>> nodeTypes = new ArrayList<>();
                if (nodeObj.has("types")) {
                    JsonArray types = nodeObj.getAsJsonArray("types");
                    for (JsonElement str : types) {
                        nodeTypes.add(AllTypes.fromJson(str));
                    }
                }

                Node node = null;
                for (Node n : NodeList.root.collectNodes()) {
                    if (n.id.equals(type)) {
                        if (nodeTypes.isEmpty()) {
                            node = n.copy();
                        } else {
                            node = n.copyWithTypes(nodeTypes);
                        }
                        break;
                    }
                }

                if (node == null && nodeObj.has("function")) {
                    FunctionDefinition function = this.functions.get(nodeObj.get("function").getAsString());
                    if (function != null) {
                        node = switch (type) {
                            case "function_inputs" -> function.inputsNode;
                            case "function_outputs" -> function.outputsNode;
                            case "function_call" -> new FunctionCallNode(function);
                            default -> null;
                        };
                    }
                }

                if (nodeObj.has("varargs")) {
                    JsonObject varargsObj = nodeObj.getAsJsonObject("varargs");
                    for (Map.Entry<String, JsonElement> entry : varargsObj.entrySet()) {
                        String varargsId = entry.getKey();
                        JsonArray children = entry.getValue().getAsJsonArray();
                        for (Varargs<?> varargs : node.varargs) {
                            varargs.ignoreUpdates = true;
                            node.inputs.removeAll(varargs.children);
                            varargs.children.clear();
                            if (varargs.id.equals(varargsId)) {
                                for (JsonElement child : children) {
                                    varargs.addInput(child.getAsString());
                                }
                                break;
                            }
                        }
                    }
                }

                NodeWidget nodeWidget = new NodeWidget(node, this);
                nodeWidget.setPos(new Vec(x, y, 15.999));
                nodeWidgets.add(nodeWidget);
                rootWidgets.add(nodeWidget);
                
                if (nodeObj.has("insets")) {
                    JsonObject insets = nodeObj.getAsJsonObject("insets");
                    for (Map.Entry<String, JsonElement> entry : insets.entrySet()) {
                        String inputId = entry.getKey();
                        String inset = entry.getValue().getAsString();
                        
                        for (NodeIOWidget io : nodeWidget.getIOWidgets()) {
                            if (io.isInput() && io.input.id.equals(inputId)) {
                                io.insetValue(inset, this);
                                break;
                            }
                        }
                    }
                }

                todo.add(() -> {
                    if (nodeObj.has("inputs")) {
                        JsonObject inputs = nodeObj.getAsJsonObject("inputs");
                        for (Map.Entry<String, JsonElement> entry : inputs.entrySet()) {
                            String inputId = entry.getKey();
                            int nodeIndex = Integer.parseInt(entry.getValue().getAsString().split(":")[0]);
                            int outputId = Integer.parseInt(entry.getValue().getAsString().split(":")[1]);

                            NodeWidget other = nodeWidgets.get(nodeIndex);
                            for (Node.Input<?> input : nodeWidget.node.inputs) {
                                if (input.id.equals(inputId)) {
                                    ((Node.Input<Object>) input).connect((Node.Output<Object>) other.node.outputs.get(outputId));
                                    break;
                                }
                            }
                        }
                    }
                    if (nodeObj.has("outputs")) {
                        JsonObject outputs = nodeObj.getAsJsonObject("outputs");
                        for (Map.Entry<String, JsonElement> entry : outputs.entrySet()) {
                            String outputId = entry.getKey();
                            int nodeIndex = Integer.parseInt(entry.getValue().getAsString().split(":")[0]);
                            int inputId = Integer.parseInt(entry.getValue().getAsString().split(":")[1]);

                            NodeWidget other = nodeWidgets.get(nodeIndex);
                            for (Node.Output<?> output : nodeWidget.node.outputs) {
                                if (output.id.equals(outputId)) {
                                    ((Node.Output<Object>) output).connected = (Node.Input<Object>) other.node.inputs.get(inputId);
                                    break;
                                }
                            }
                        }
                    }

                    for (Varargs<?> varargs : nodeWidget.node.varargs) {
                        varargs.ignoreUpdates = false;
                        varargs.update();
                    }
                    nodeWidget.update(space.code);
                });
            }
            
            JsonArray wires = data.getAsJsonArray("wires");
            List<WireWidget> wireWidgets = new ArrayList<>();
            
            for (JsonElement wireElem : wires) {
                JsonObject wireObj = wireElem.getAsJsonObject();
                JsonElement type = wireObj.get("type");
                double fromX = wireObj.get("fromX").getAsDouble();
                double fromY = wireObj.get("fromY").getAsDouble();
                double toX = wireObj.get("toX").getAsDouble();
                double toY = wireObj.get("toY").getAsDouble();
                
                WireType<?> typeInst = AllTypes.fromJson(type);
                WireWidget wire = new WireWidget(typeInst, new Vec(fromX, fromY, 15.999), new Vec(toX, toY, 15.999));
                if (wireObj.has("previousOutputNode") && wireObj.has("previousOutputId")) {
                    int nodeIndex = wireObj.get("previousOutputNode").getAsInt();
                    String outputId = wireObj.get("previousOutputId").getAsString();
                    for (NodeIOWidget io : nodeWidgets.get(nodeIndex).getIOWidgets()) {
                        if (!io.isInput() && io.output.id.equals(outputId)) {
                            wire.setPreviousOutput(io);
                            io.connections.add(wire);
                            break;
                        }
                    }
                }

                if (wireObj.has("nextInputId") && wireObj.has("nextInputNode")) {
                    int nodeIndex = wireObj.get("nextInputNode").getAsInt();
                    String inputId = wireObj.get("nextInputId").getAsString();
                    for (NodeIOWidget io : nodeWidgets.get(nodeIndex).getIOWidgets()) {
                        if (io.isInput() && io.input.id.equals(inputId)) {
                            wire.setNextInput(io);
                            io.connections.add(wire);
                            break;
                        }
                    }
                }

                todo.add(() -> {
                    if (wireObj.has("previousWires")) {
                        for (JsonElement previousWireElem : wireObj.getAsJsonArray("previousWires")) {
                            int index = previousWireElem.getAsInt();
                            wire.previousWires.add(wireWidgets.get(index));
                        }
                    }
                    if (wireObj.has("nextWires")) {
                        for (JsonElement nextWireElem : wireObj.getAsJsonArray("nextWires")) {
                            int index = nextWireElem.getAsInt();
                            wire.nextWires.add(wireWidgets.get(index));
                        }
                    }
                    wire.update(space.code);
                });

                wireWidgets.add(wire);
                rootWidgets.add(wire);
            }

            for (Runnable item : todo) {
                item.run();
            }
        } catch (IOException e) {
            FireFlow.LOGGER.error("Failed to load code.json for space " + space.info.id + "!", e);
        }
    }
}
