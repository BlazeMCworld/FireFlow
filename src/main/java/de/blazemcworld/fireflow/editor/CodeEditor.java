package de.blazemcworld.fireflow.editor;

import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.compiler.FunctionDefinition;
import de.blazemcworld.fireflow.compiler.StructDefinition;
import de.blazemcworld.fireflow.editor.action.MoveSelectionAction;
import de.blazemcworld.fireflow.editor.widget.CreateWidget;
import de.blazemcworld.fireflow.editor.widget.NodeInputWidget;
import de.blazemcworld.fireflow.editor.widget.NodeWidget;
import de.blazemcworld.fireflow.editor.widget.WireWidget;
import de.blazemcworld.fireflow.editor.action.DeleteSelectionAction;
import de.blazemcworld.fireflow.node.*;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.util.PlayerExitInstanceEvent;
import de.blazemcworld.fireflow.value.AllValues;
import de.blazemcworld.fireflow.value.SignalValue;
import de.blazemcworld.fireflow.value.StructValue;
import de.blazemcworld.fireflow.value.Value;
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
import net.minestom.server.instance.Instance;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class CodeEditor {

    public final Instance inst;
    public final List<Widget> widgets = new ArrayList<>();
    private final HashMap<Player, EditorAction> actions = new HashMap<>();
    private final Path filePath;
    public final List<FunctionDefinition> functions = new ArrayList<>();
    public final List<StructDefinition> structs = new ArrayList<>();

    public CodeEditor(Space space) {
        filePath = Path.of("spaces").resolve(String.valueOf(space.info.id)).resolve("code.bin");
        inst = space.code;
        load();

        EventNode<InstanceEvent> events = inst.eventNode();

        events.addListener(PlayerSpawnEvent.class, event -> {
            Player player = event.getPlayer();
            player.setAllowFlying(true);
            player.setFlying(true);

            Entity helper = new Entity(EntityType.INTERACTION);
            helper.setNoGravity(true);
            InteractionMeta meta = (InteractionMeta) helper.getEntityMeta();
            meta.setWidth(-0.5f);
            meta.setHeight(-0.5f);
            helper.setInstance(inst, Pos.ZERO);
            player.addPassenger(helper);
        });

        events.addListener(PlayerExitInstanceEvent.class, event -> {
            for (Entity passenger : event.getPlayer().getPassengers()) {
                if (passenger.getEntityType() == EntityType.INTERACTION) {
                    passenger.remove();
                }
            }
            if (actions.containsKey(event.getPlayer())) {
                setAction(event.getPlayer(), null);
            }
        });

        events.addListener(PlayerEntityInteractEvent.class, event -> {
            Vec cursor = getCursor(event.getPlayer());
            if (actions.containsKey(event.getPlayer())) {
                actions.get(event.getPlayer()).rightClick(cursor);
                return;
            }
            Widget selected = getWidget(event.getPlayer(), cursor);
            if (selected == null) {
                this.setAction(event.getPlayer(), new MoveSelectionAction(inst, cursor, event.getPlayer(), this));
                return;
            }
            selected.rightClick(cursor, event.getPlayer(), this);
        });

        events.addListener(PlayerSwapItemEvent.class, event -> {
            event.setCancelled(true);
            Vec cursor = getCursor(event.getPlayer());
            if (actions.containsKey(event.getPlayer())) {
                actions.get(event.getPlayer()).swapItem(cursor);
                return;
            }
            Widget selected = getWidget(event.getPlayer(), cursor);
            if (selected == null) {
                widgets.add(new CreateWidget(cursor, inst));
                return;
            }
            selected.swapItem(cursor, event.getPlayer(), this);
        });

        events.addListener(EntityAttackEvent.class, event -> {
            if (event.getEntity() instanceof Player player) {
                Vec cursor = getCursor(player);
                if (actions.containsKey(player)) {
                    actions.get(player).leftClick(cursor);
                    return;
                }
                Widget selected = getWidget(player, cursor);
                if (selected == null) {
                    this.setAction(player, new DeleteSelectionAction(inst, cursor, player, this));
                    return;
                }
                selected.leftClick(cursor, player, this);
            }
        });

        events.addListener(PlayerTickEvent.class, event -> {
            if (!actions.containsKey(event.getPlayer())) return;
            Vec cursor = getCursor(event.getPlayer());
            actions.get(event.getPlayer()).tick(cursor);
        });

        events.addListener(PlayerChatEvent.class, event -> {
            Vec cursor = getCursor(event.getPlayer());
            if (actions.containsKey(event.getPlayer())) {
                actions.get(event.getPlayer()).chat(cursor, event);
                return;
            }
            Widget selected = getWidget(event.getPlayer(), cursor);
            if (selected == null) return;
            selected.chat(cursor, event, this);
        });
    }

    @Nullable
    public Widget getWidget(Player player, Vec cursor) {
        for (Widget w : widgets) {
            Widget res = w.select(player, cursor);
            if (res != null) return res;
        }
        return null;
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


    public void remove(Widget widget) {
        widget.remove();
        widgets.remove(widget);
    }

    public void setAction(Player player, EditorAction action) {
        if (actions.containsKey(player)) {
            actions.get(player).stop();
        }
        if (action == null) actions.remove(player);
        else actions.put(player, action);
    }

    public void save() {
        NetworkBuffer buffer = new NetworkBuffer();
        buffer.write(NetworkBuffer.INT, 2); // version

        buffer.write(NetworkBuffer.INT, structs.size());
        for (StructDefinition st : structs) {
            buffer.write(NetworkBuffer.STRING, st.stName);
            buffer.write(NetworkBuffer.BYTE, (byte) st.type.fields.size());
            for (StructValue.Field field : st.type.fields) {
                buffer.write(NetworkBuffer.STRING, field.name());
                AllValues.writeValue(buffer, field.type());
            }
        }

        buffer.write(NetworkBuffer.INT, functions.size());
        for (FunctionDefinition fn : functions) {
            buffer.write(NetworkBuffer.STRING, fn.fnName);

            buffer.write(NetworkBuffer.INT, fn.fnInputs.size());
            for (NodeOutput input : fn.fnInputs) {
                buffer.write(NetworkBuffer.STRING, input.getName());
                AllValues.writeValue(buffer, input.type);
            }

            buffer.write(NetworkBuffer.INT, fn.fnOutputs.size());
            for (NodeInput output : fn.fnOutputs) {
                buffer.write(NetworkBuffer.STRING, output.getName());
                AllValues.writeValue(buffer, output.type);
            }
        }

        List<NodeWidget> nodes = new ArrayList<>();
        for (Widget w : widgets) {
            if (w instanceof NodeWidget node) nodes.add(node);
        }

        buffer.write(NetworkBuffer.INT, nodes.size());
        for (NodeWidget n : nodes) {
            buffer.write(NetworkBuffer.STRING, n.node.getBaseName());
            byte type = 0;
            if (n.node instanceof FunctionDefinition.Call) type = 1;
            if (n.node instanceof FunctionDefinition.DefinitionNode d) type = (byte) (d.getDefinition().fnInputsNode == d ? 2 : 3);
            buffer.write(NetworkBuffer.BYTE, type);
            n.node.writeData(buffer);
            buffer.write(NetworkBuffer.DOUBLE, n.origin.x());
            buffer.write(NetworkBuffer.DOUBLE, n.origin.y());

            buffer.write(NetworkBuffer.INT, n.inputs.size());
            for (NodeInputWidget i : n.inputs) {
                buffer.write(NetworkBuffer.INT, i.wires.size());
                for (WireWidget wire : i.wires) {
                    buffer.write(NetworkBuffer.INT, nodes.indexOf(wire.output.parent));
                    buffer.write(NetworkBuffer.INT, wire.output.parent.outputs.indexOf(wire.output));

                    buffer.write(NetworkBuffer.INT, wire.relays.size());
                    for (Vec relay : wire.relays) {
                        buffer.write(NetworkBuffer.DOUBLE, relay.x());
                        buffer.write(NetworkBuffer.DOUBLE, relay.y());
                    }
                }
            }
        }

        try {
            if (!Files.exists(filePath.getParent())) Files.createDirectories(filePath.getParent());
            Files.write(filePath, buffer.readBytes(buffer.writeIndex()));
        } catch (IOException err) {
            FireFlow.LOGGER.error("Failed to save code file!", err);
        }
    }

    public void load() {
        for (Widget w : widgets) {
            w.remove();
        }
        widgets.clear();

        NetworkBuffer buffer;
        int length;
        try {
            if (!Files.exists(filePath)) return;
            ByteBuffer nioBuffer = ByteBuffer.wrap(Files.readAllBytes(filePath));
            buffer = new NetworkBuffer(nioBuffer);
            length = nioBuffer.capacity();
        } catch (IOException err) {
            FireFlow.LOGGER.error("Failed to read code file!", err);
            return;
        }

        int version = buffer.read(NetworkBuffer.INT);

        for (CodeMigration m : CodeMigration.values()) buffer = m.apply(version, length, buffer);

        structs.clear();
        int stCount = buffer.read(NetworkBuffer.INT);
        for (int stId = 0; stId < stCount; stId++) {
            String name = buffer.read(NetworkBuffer.STRING);
            byte len = buffer.read(NetworkBuffer.BYTE);
            ArrayList<StructValue.Field> fields = new ArrayList<>(len);
            for (byte i = 0; i < len; i++) fields.add(new StructValue.Field(buffer.read(NetworkBuffer.STRING), AllValues.readValue(buffer, List.of())));
            StructValue type = new StructValue(name, fields);
            structs.add(new StructDefinition(type));
        }

        functions.clear();
        int fnCount = buffer.read(NetworkBuffer.INT);
        for (int fnId = 0; fnId < fnCount; fnId++) {
            String name = buffer.read(NetworkBuffer.STRING);

            int count = buffer.read(NetworkBuffer.INT);
            List<NodeOutput> inputs = new ArrayList<>(count);
            for (int each = 0; each < count; each++) {
                String ioName = buffer.read(NetworkBuffer.STRING);
                Value type = AllValues.readValue(buffer, structs);
                inputs.add(new NodeOutput(ioName, type));
            }

            count = buffer.read(NetworkBuffer.INT);
            List<NodeInput> outputs = new ArrayList<>(count);
            for (int each = 0; each < count; each++) {
                String ioName = buffer.read(NetworkBuffer.STRING);
                Value type = AllValues.readValue(buffer, structs);
                outputs.add(new NodeInput(ioName, type));
            }

            functions.add(new FunctionDefinition(name, inputs, outputs));
        }

        List<Runnable> connectNodes = new ArrayList<>();

        int nodeCount = buffer.read(NetworkBuffer.INT);
        for (int nodeId = 0; nodeId < nodeCount; nodeId++) {
            String id = buffer.read(NetworkBuffer.STRING);
            byte type = 0;
            if (version >= 1) type = buffer.read(NetworkBuffer.BYTE);

            Supplier<Node> supplier = null;
            if (type == 0) {
                supplier = NodeList.nodes.get(id);
            } else if (type >= 1 && type <= 3) {
                for (FunctionDefinition fn : functions) {
                    if (!fn.fnName.equals(id)) continue;
                    if (type == 1) {
                        supplier = fn::createCall;
                    } else if (type == 2) {
                        supplier = () -> fn.fnInputsNode;
                    } else {
                        supplier = () -> fn.fnOutputsNode;
                    }
                    break;
                }
            }

            if (supplier == null) {
                widgets.add(null);
                continue;
            }
            Node node = supplier.get();
            node = node.readData(buffer, structs);
            double x = buffer.read(NetworkBuffer.DOUBLE);
            double y = buffer.read(NetworkBuffer.DOUBLE);

            NodeWidget widget = new NodeWidget(new Vec(x, y, 15.999), inst, node);
            widget.update(false);

            int inputCount = buffer.read(NetworkBuffer.INT);
            for (int inputId = 0; inputId < inputCount; inputId++) {

                int wireCount = buffer.read(NetworkBuffer.INT);
                for (int wireId = 0; wireId < wireCount; wireId++) {
                    int outNode = buffer.read(NetworkBuffer.INT);
                    int output = buffer.read(NetworkBuffer.INT);

                    int relayCount = buffer.read(NetworkBuffer.INT);
                    List<Vec> relays = new ArrayList<>();
                    for (int l = 0; l < relayCount; l++) {
                        relays.add(new Vec(buffer.read(NetworkBuffer.DOUBLE), buffer.read(NetworkBuffer.DOUBLE), 15.999));
                    }

                    int currentInputId = inputId;
                    connectNodes.add(() -> {
                        if (widgets.get(outNode) instanceof NodeWidget out) {
                            widget.inputs.get(currentInputId).addWire(new WireWidget(
                                    inst, widget.inputs.get(currentInputId), out.outputs.get(output), relays
                            ));
                            if (widget.node.inputs.get(currentInputId).type == SignalValue.INSTANCE) {
                                out.node.outputs.get(output).connectSignal(widget.node.inputs.get(currentInputId));
                            } else {
                                widget.node.inputs.get(currentInputId).connectValue(out.node.outputs.get(output));
                            }
                        }
                    });
                }
            }

            widgets.add(widget);
        }

        for (Runnable r : connectNodes) r.run();

        while (widgets.contains(null)) widgets.remove(null);
    }

    public List<Node> getNodes() {
        List<Node> list = new ArrayList<>();
        for (Widget w : widgets) {
            if (w instanceof NodeWidget node) list.add(node.node);
        }
        return list;
    }

    public List<NodeWidget> getNodesInBound(Bounds bounds) {
        List<NodeWidget> list = new ArrayList<>();
        for (Widget w : widgets) {
            if (w instanceof NodeWidget node) {
                Bounds nodeBounds = node.getBounds();
                if (bounds.includes2d(nodeBounds.min) && bounds.includes2d(nodeBounds.max)) list.add(node);
            }
        }
        return list;
    }

    public void redefineFunc(FunctionDefinition prev, FunctionDefinition next) {
        if (!functions.contains(prev)) return;
        functions.remove(prev);
        functions.add(next);
        ArrayList<Widget> addMe = new ArrayList<>();
        ArrayList<Widget> removeMe = new ArrayList<>();
        for (Widget w : widgets) {
            if (w instanceof NodeWidget n && n.node instanceof FunctionDefinition.DefinitionNode d) {
                if (d.getDefinition() != prev) continue;
                if (n.node == prev.fnInputsNode) {
                    addMe.add(new NodeWidget(n.origin, inst, next.fnInputsNode));
                }
                if (n.node == prev.fnOutputsNode) {
                    addMe.add(new NodeWidget(n.origin, inst, next.fnOutputsNode));
                }
                removeMe.add(w);
            }
        }
        for (Widget w : removeMe) remove(w);
        widgets.addAll(addMe);
    }

    public void redefineStruct(StructDefinition prev, StructDefinition next) {
        if (!structs.contains(prev)) return;
        structs.remove(prev);
        structs.add(next);
        Widget addMe = null;
        Widget removeMe = null;
        for (Widget w : widgets) {
            if (w instanceof NodeWidget n && n.node instanceof StructDefinition.InitializationNode i) {
                if (i.getDefinition() == prev) {
                    if (n.node == prev.initNode) addMe = new NodeWidget(n.origin, inst, next.initNode);
                    removeMe = n;
                    break;
                }
            }
        }
        if (removeMe != null) remove(removeMe);
        if (addMe != null) widgets.add(addMe);
    }

    public void removeFunc(FunctionDefinition fn) {
        if (!functions.contains(fn)) return;
        functions.remove(fn);
        List<Widget> removeMe = new ArrayList<>();
        for (Widget w : widgets) {
            if (w instanceof NodeWidget n && n.node instanceof FunctionDefinition.DefinitionNode d) {
                if (d.getDefinition() != fn) continue;
                removeMe.add(n);
            }
        }
        for (Widget w : removeMe) remove(w);
    }

    public void removeStruct(StructDefinition st) {
        if (!structs.contains(st)) return;
        structs.remove(st);
        Widget removeMe = null;
        for (Widget w : widgets) {
            if (w instanceof NodeWidget n && n.node instanceof StructDefinition.InitializationNode i) {
                if (i.getDefinition() == st) {
                    removeMe = n;
                    break;
                }
            }
        }
        if (removeMe != null) remove(removeMe);
    }

    public boolean funcInUse(FunctionDefinition check) {
        for (Widget w : widgets) {
            if (w instanceof NodeWidget n && n.node instanceof FunctionDefinition.Call c) {
                if (c.getDefinition() == check) return true;
            }
        }
        return false;
    }

    public boolean structInUse(StructDefinition check) {
        for (Widget w : widgets) {
            if (w instanceof NodeWidget n && n.node instanceof StructDefinition.Create c) {
                if (c.getDefinition() == check) return true;
            }
        }
        return false;
    }
}
