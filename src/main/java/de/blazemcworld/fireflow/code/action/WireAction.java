package de.blazemcworld.fireflow.code.action;

import de.blazemcworld.fireflow.code.CodeEditor;
import de.blazemcworld.fireflow.code.CodeInteraction;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.WireType;
import de.blazemcworld.fireflow.code.widget.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class WireAction implements CodeAction {
    private NodeIOWidget input;
    private NodeIOWidget output;
    private WireWidget inputWire;
    private WireWidget outputWire;
    private WidgetVec startPos;
    private List<WireWidget> wires = new ArrayList<>();
    private List<List<WireWidget>> permanentWires = new ArrayList<>();
    private WireWidget startWire;
    private WireWidget endWire;

    public WireAction(NodeIOWidget io, CodeEditor editor, ServerPlayerEntity player) {
        if (!io.isInput()) {
            output = io;
            startPos = io.pos().sub(output.size().sub(-1 / 4f, 1 / 8f));
            startWire = new WireWidget(io.pos().sub(output.size().sub(1 / 8f, 1 / 8f)), output.type(), startPos);
            editor.lockWidget(io.parent, player);
        }
//        else {
//            input = io;
//            startPos = io.pos().sub(-1 / 4f, 1 / 8f, 0);
//            startWire = new WireWidget(io.pos().sub(1 / 8f - 1 / 32f, 1 / 8f, 0), input.type(), startPos);
//        }
    }

    public WireAction(WireWidget wire, WidgetVec cursor, ServerPlayerEntity player) {
        inputWire = wire;
        startPos = cursor;
        if (inputWire.line.from.x() == inputWire.line.to.x()) {
            startPos = startPos.withX(inputWire.line.from.x());
        } else {
            startPos = startPos.withY(inputWire.line.from.y());
        }
        wire.lockWire(player);
    }

    @Override
    public void tick(WidgetVec cursor, ServerPlayerEntity player) {
        if (startWire != null) startWire.update();
        WidgetVec endPos = cursor.gridAligned();
        NodeIOWidget hover = null;
        WireType<?> type = (output != null) ? output.type() : inputWire.type();
        NodeIOWidget io = cursor.editor().selectIOWidget(cursor);
        if (io != null && io.isInput() && io.input.canUnderstand(type)) {
            endPos = io.pos().sub(-1 / 4f, 1 / 8f);
            hover = io;
        }
        cursor = cursor.gridAligned();
        if (wires.isEmpty()) {
            WireWidget lastWire = new WireWidget(startPos, type, startPos);
            lastWire.update();
            wires.add(lastWire);
        }
        List<WidgetVec> positions;
        if (inputWire != null) {
            if (inputWire.line.from.x() != wires.getFirst().line.from.x())
                wires.getFirst().line.to = wires.getFirst().line.to.withY(endPos.y());
            else wires.getFirst().line.to = wires.getFirst().line.to.withX(endPos.x());
            positions = cursor.editor().pathfinder.findPath(wires.getFirst().line.to, endPos);
            if (wires.size() == 1) {
                WireWidget lastWire = new WireWidget(wires.getFirst(), type, startPos);
                lastWire.update();
                wires.add(lastWire);
            }
        } else {
            positions = cursor.editor().pathfinder.findPath(startPos, endPos);
        }
        int index = (inputWire != null) ? 1 : 0;
        for (WidgetVec position : positions) {
            WireWidget lastWire = wires.get(index);
            if (index == wires.size() - 1) {
                WireWidget wire = new WireWidget(lastWire, type, position);
                wire.update();
                wires.add(wire);
            } else {
                wires.get(index + 1).line.from = lastWire.line.to;
                wires.get(index + 1).line.to = position;
                wires.get(index + 1).update();
            }
            index++;
        }

        for (int i = index + 1; i < wires.size(); i++) {
            wires.get(i).remove();
        }
        wires = wires.subList(0, index + 1);

        if (hover != null) {
            WireWidget lastWire = wires.get(index);
            if (endWire == null) endWire = new WireWidget(lastWire, type, hover.pos());
            endWire.line.from = lastWire.line.to;
            endWire.line.to = (hover.isInput()) ? hover.pos().sub(1 / 8f, 1 / 8f) : hover.pos().sub(hover.size().sub(1 / 8f, 1 / 8f));
            endWire.update();
        } else if (endWire != null) {
            endWire.remove();
            endWire = null;
        }
    }

    @Override
    public boolean interact(CodeInteraction i) {
        if (i.type() == CodeInteraction.Type.RIGHT_CLICK) {
            NodeIOWidget io = i.pos().editor().selectIOWidget(i.pos());
            if (io != null) {
                WireType<?> type = (output != null) ? output.type() : inputWire.type();
                if (endWire == null) return false;
                if (!io.isInput()) return false;
                if (!io.input.canUnderstand(type)) return false;
                if (!io.connections.isEmpty() && type != SignalType.INSTANCE) return false;
                if (i.pos().editor().isLocked(io.parent) != null && !i.pos().editor().isLockedByPlayer(io.parent, i.player())) {
                    i.player().sendMessage(Text.literal("This widget is currently in use by another player!"));
                    return true;
                }
                input = io;

                for (int j = 0; j < permanentWires.size(); j++) {
                    if (j == 0) continue;
                    permanentWires.get(j - 1).getLast().connectNext(permanentWires.get(j).getFirst());
                }
                if (!permanentWires.isEmpty()) permanentWires.getLast().getLast().connectNext(wires.getFirst());

                WireWidget firstWire = wires.getFirst();
                if (!permanentWires.isEmpty()) firstWire = permanentWires.getFirst().getFirst();

                if (output != null) {
                    startWire.connectNext(firstWire);
                    startWire.connectPrevious(output);
                    i.pos().editor().rootWidgets.add(startWire);
                } else {
                    List<WireWidget> wires = inputWire.splitWire(firstWire.line.from);
                    wires.getFirst().connectNext(firstWire);
                }

                endWire.connectNext(input);
                i.pos().editor().rootWidgets.add(endWire);

                for (List<WireWidget> list : permanentWires) {
                    for (WireWidget wire : list) {
                        i.pos().editor().rootWidgets.add(wire);
                    }
                }

                for (WireWidget wire : wires) {
                    i.pos().editor().rootWidgets.add(wire);
                }

                if (output != null) output.connect(startWire);
                input.connect(endWire);
                endWire.cleanup();
                permanentWires = null;
                wires = null;
                startWire = null;
                endWire = null;
                i.pos().editor().stopAction(i.player());
                return true;
            }

            for (Widget widget : i.pos().editor().rootWidgets) {
                if (!(widget instanceof NodeWidget n)) continue;
                for (NodeIOWidget it : n.getIOWidgets()) {
                    for (WireWidget wireWidget : it.connections) {
                        if (!wireWidget.inBounds(i.pos())) continue;
                        WireType<?> type = output.type();
                        if (wireWidget.type() != type) return false;
                        if (type != SignalType.INSTANCE) return false;

                        for (int j = 0; j < permanentWires.size(); j++) {
                            if (j == 0) continue;
                            permanentWires.get(j - 1).getLast().connectNext(permanentWires.get(j).getFirst());
                        }
                        if (!permanentWires.isEmpty())
                            permanentWires.getLast().getLast().connectNext(wires.getFirst());

                        for (List<WireWidget> list : permanentWires) {
                            for (WireWidget wire : list) {
                                i.pos().editor().rootWidgets.add(wire);
                            }
                        }

                        for (WireWidget wire : wires) {
                            i.pos().editor().rootWidgets.add(wire);
                        }

                        List<WireWidget> wires = wireWidget.splitWire(i.pos());
                        wires.getLast().connectPrevious(this.wires.getLast());

                        WireWidget firstWire = this.wires.getFirst();
                        if (!permanentWires.isEmpty()) firstWire = permanentWires.getFirst().getFirst();
                        startWire.connectNext(firstWire);
                        startWire.connectPrevious(output);
                        i.pos().editor().rootWidgets.add(startWire);
                        output.connect(startWire);
                        startWire.cleanup();
                        permanentWires = null;
                        this.wires = null;
                        startWire = null;
                        endWire = null;
                        i.pos().editor().stopAction(i.player());
                        return true;
                    }
                }
            }

            permanentWires.add(new ArrayList<>(wires));
            startPos = wires.getLast().line.to;
            wires = new ArrayList<>();
        } else if (i.type() == CodeInteraction.Type.LEFT_CLICK) {
            if (permanentWires.isEmpty()) {
                i.pos().editor().stopAction(i.player());
                return true;
            }
            for (WireWidget wire : wires) {
                wire.remove();
            }
            wires = permanentWires.removeLast();
            if (permanentWires.isEmpty())
                startPos = input != null ? input.pos().sub(-1 / 4f, 1 / 8f) : output != null ? output.pos().sub(output.size().sub(-1 / 4f, 1 / 8f)) : wires.getFirst().line.from;
            else startPos = permanentWires.getLast().getLast().line.to;
        }
        return false;
    }

    @Override
    public void stop(CodeEditor editor, ServerPlayerEntity player) {
        if (permanentWires != null) {
            for (List<WireWidget> list : permanentWires) {
                for (WireWidget wire : list) {
                    wire.remove();
                }
            }
        }
        if (wires != null) {
            for (WireWidget wire : wires) {
                wire.remove();
            }
        }
        if (startWire != null) startWire.remove();
        if (endWire != null) endWire.remove();
        if (permanentWires != null && !permanentWires.isEmpty()) permanentWires.clear();
        if (wires != null && !wires.isEmpty()) wires.clear();

        if (output != null || input != null) editor.unlockWidget((output != null ? output : input).parent, player);
        if (inputWire != null) inputWire.unlockWire(player);
    }
}
