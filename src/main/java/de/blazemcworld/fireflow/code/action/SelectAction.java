package de.blazemcworld.fireflow.code.action;

import de.blazemcworld.fireflow.code.CodeEditor;
import de.blazemcworld.fireflow.code.CodeInteraction;
import de.blazemcworld.fireflow.code.EditOrigin;
import de.blazemcworld.fireflow.code.widget.RectElement;
import de.blazemcworld.fireflow.code.widget.Widget;
import de.blazemcworld.fireflow.code.widget.WidgetVec;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.List;

public class SelectAction implements CodeAction {
    final RectElement box;

    public SelectAction(WidgetVec pos) {
        box = new RectElement(pos);
        box.pos = pos;
        box.size = new WidgetVec(pos.editor(), 0, 0);
        box.color(TextColor.fromFormatting(Formatting.AQUA));
    }

    @Override
    public void tick(WidgetVec cursor, EditOrigin player) {
        box.size = box.pos.sub(cursor);
        box.update();
    }

    @Override
    public boolean interact(CodeInteraction i) {
        List<Widget> widgets = i.pos().editor().getAllWidgetsBetween(i, box.pos, i.pos());
        if (i.type() == CodeInteraction.Type.LEFT_CLICK || widgets.isEmpty()) i.pos().editor().stopAction(i.origin());
        else if (i.type() == CodeInteraction.Type.RIGHT_CLICK) {
            i.pos().editor().stopAction(i.origin());
            i.pos().editor().setAction(i.origin(), new DragSelectionAction(widgets, i.pos(), i.origin()));
            return true;
        } else if (i.type() == CodeInteraction.Type.SWAP_HANDS) {
            i.pos().editor().stopAction(i.origin());
            i.pos().editor().setAction(i.origin(), new CopySelectionAction(widgets, i.pos()));
            return true;
        }
        return false;
    }

    @Override
    public void stop(CodeEditor editor, EditOrigin player) {
        box.remove();
    }
}
