package de.blazemcworld.fireflow.code.action;

import de.blazemcworld.fireflow.code.CodeEditor;
import de.blazemcworld.fireflow.code.CodeInteraction;
import de.blazemcworld.fireflow.code.widget.WidgetVec;
import de.blazemcworld.fireflow.code.widget.WireWidget;
import net.minecraft.server.network.ServerPlayerEntity;

public class DragWireAction implements CodeAction {
    private final WireWidget wire;

    public DragWireAction(WireWidget wire, CodeEditor editor, ServerPlayerEntity player) {
        this.wire = wire;
        wire.lockWire(player);
    }

    @Override
    public void tick(WidgetVec cursor, ServerPlayerEntity player) {
        cursor = cursor.gridAligned();
        if (wire.line.from.y() != wire.line.to.y()) {
            wire.line.from = wire.line.from.withX(cursor.x());
            wire.line.to = wire.line.to.withX(cursor.x());
        } else {
            wire.line.from = wire.line.from.withY(cursor.y());
            wire.line.to = wire.line.to.withY(cursor.y());
        }
        moveWire(wire, null);
        wire.update();
    }

    private void moveWire(WireWidget wire, WireWidget avoid) {
        if (wire.line.from.y() != wire.line.to.y()) {
            for (WireWidget wireWidget : wire.previousWires) {
                if (wireWidget == avoid) continue;
                wireWidget.line.to = wire.line.from;
                if (wireWidget.line.from.y() != wireWidget.line.to.y()) wireWidget.line.from = wireWidget.line.from.withX(wireWidget.line.to.x());
                else wireWidget.line.from = wireWidget.line.from.withY(wireWidget.line.to.y());
                moveWire(wireWidget, wire);
            }
            for (WireWidget wireWidget : wire.nextWires) {
                if (wireWidget == avoid) continue;
                wireWidget.line.from = wire.line.to;
                if (wireWidget.line.from.y() != wireWidget.line.to.y()) wireWidget.line.to = wireWidget.line.to.withX(wireWidget.line.from.x());
                else wireWidget.line.to = wireWidget.line.to.withY(wireWidget.line.from.y());
                moveWire(wireWidget, wire);
            }
        } else {
            for (WireWidget wireWidget : wire.previousWires) {
                if (wireWidget == avoid) continue;
                wireWidget.line.to = wire.line.from;
                if (wireWidget.line.from.x() != wireWidget.line.to.x()) wireWidget.line.from = wireWidget.line.from.withY(wireWidget.line.to.y());
                else wireWidget.line.from = wireWidget.line.from.withX(wireWidget.line.to.x());
                moveWire(wireWidget, wire);
            }
            for (WireWidget wireWidget : wire.nextWires) {
                if (wireWidget == avoid) continue;
                wireWidget.line.from = wire.line.to;
                if (wireWidget.line.from.x() != wireWidget.line.to.x()) wireWidget.line.to = wireWidget.line.to.withY(wireWidget.line.from.y());
                else wireWidget.line.to = wireWidget.line.to.withX(wireWidget.line.from.x());
                moveWire(wireWidget, wire);
            }
        }
        wire.update();
    }

    @Override
    public boolean interact(CodeInteraction i) {
        if (i.type() == CodeInteraction.Type.RIGHT_CLICK) {
            i.pos().editor().stopAction(i.player());
            return true;
        }
        return false;
    }

    @Override
    public void stop(CodeEditor editor, ServerPlayerEntity player) {
        wire.unlockWire(player);
        wire.cleanup();
    }
}
