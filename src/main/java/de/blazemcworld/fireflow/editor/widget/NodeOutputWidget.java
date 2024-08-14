package de.blazemcworld.fireflow.editor.widget;

import de.blazemcworld.fireflow.compiler.FunctionDefinition;
import de.blazemcworld.fireflow.compiler.StructDefinition;
import de.blazemcworld.fireflow.editor.CodeEditor;
import de.blazemcworld.fireflow.node.NodeOutput;
import de.blazemcworld.fireflow.util.Messages;
import de.blazemcworld.fireflow.value.SignalValue;
import de.blazemcworld.fireflow.value.StructValue;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.instance.Instance;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NodeOutputWidget extends ButtonWidget {

    public final NodeOutput output;
    public final NodeWidget parent;
    public Set<NodeInputWidget> connected = new HashSet<>();

    public NodeOutputWidget(Vec position, Instance inst, Component text, NodeOutput output, NodeWidget parent) {
        super(position, inst, text);
        this.output = output;
        this.parent = parent;
    }

    public void disconnect() {
        for (NodeInputWidget each : connected) {
            each.wires.removeIf(widget -> {
                if (widget.output != this) return false;
                if (output.type != SignalValue.INSTANCE) widget.input.input.inset(null);
                widget.remove();
                return true;
            });
        }
        connected.clear();
        if (output.type == SignalValue.INSTANCE) output.connectSignal(null);
    }

    @Override
    public void chat(Vec cursor, PlayerChatEvent event, CodeEditor editor) {
        if (parent.node instanceof FunctionDefinition.DefinitionNode def) {
            event.setCancelled(true);
            FunctionDefinition prev = def.getDefinition();
            if (editor.funcInUse(prev)) {
                event.getPlayer().sendMessage(Messages.error("Can't rename inputs of used functions!"));
                return;
            }
            List<NodeOutput> updated = new ArrayList<>(prev.fnInputs);
            int id = updated.indexOf(output);
            if (id == -1) return;
            updated.set(id, new NodeOutput(event.getMessage(), output.type));
            FunctionDefinition next = new FunctionDefinition(prev.fnName, updated, prev.fnOutputs);
            editor.redefineFunc(prev, next);
        } else if (parent.node instanceof StructDefinition.InitializationNode init) {
            event.setCancelled(true);
            StructDefinition prev = init.getDefinition();
            if (editor.structInUse(prev)) {
                event.getPlayer().sendMessage(Messages.error("Can't rename inputs of used structs!"));
                return;
            }
            int id = init.outputs.indexOf(output);
            if (id == -1) return;
            ArrayList<StructValue.Field> updatedFields = new ArrayList<>(prev.type.fields);
            updatedFields.set(id - 1, new StructValue.Field(event.getMessage(), output.type));
            StructDefinition next = new StructDefinition(new StructValue(prev.stName, updatedFields));
            editor.redefineStruct(prev, next);
        }
    }

    @Override
    public void leftClick(Vec cursor, Player player, CodeEditor editor) {
        if (parent.node instanceof FunctionDefinition.DefinitionNode def) {
            FunctionDefinition prev = def.getDefinition();
            if (editor.funcInUse(prev)) {
                player.sendMessage(Messages.error("Can't delete inputs of used functions!"));
                return;
            }
            List<NodeOutput> updated = new ArrayList<>(prev.fnInputs);
            updated.remove(output);
            FunctionDefinition next = new FunctionDefinition(prev.fnName, updated, prev.fnOutputs);
            editor.redefineFunc(prev, next);
            return;
        } else if (parent.node instanceof StructDefinition.InitializationNode init) {
            StructDefinition prev = init.getDefinition();
            if (editor.structInUse(prev)) {
                player.sendMessage(Messages.error("Can't delete inputs of used structs!"));
                return;
            }
            if (output.type == SignalValue.INSTANCE) {
                player.sendMessage(Messages.error("Can't delete signal of struct initialization node!"));
                return;
            }
            int id = init.outputs.indexOf(output);
            ArrayList<StructValue.Field> updatedFields = new ArrayList<>(prev.type.fields);
            updatedFields.remove(id - 1);
            StructDefinition next = new StructDefinition(new StructValue(prev.stName, updatedFields));
            editor.redefineStruct(prev, next);
        }
        super.leftClick(cursor, player, editor);
    }

    @Override
    public void update() {
        super.update();
        if (this.connected == null) return;
        for (NodeInputWidget each : connected) {
            for (WireWidget wire : each.wires) {
                if (wire.output == this) wire.update();
            }
        }
    }

    @Override
    public void remove() {
        super.remove();
        disconnect();
    }
}
