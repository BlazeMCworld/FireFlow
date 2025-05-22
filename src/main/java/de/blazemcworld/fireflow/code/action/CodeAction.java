package de.blazemcworld.fireflow.code.action;

import de.blazemcworld.fireflow.code.CodeEditor;
import de.blazemcworld.fireflow.code.CodeInteraction;
import de.blazemcworld.fireflow.code.EditOrigin;
import de.blazemcworld.fireflow.code.widget.WidgetVec;

public interface CodeAction {
    default void stop(CodeEditor editor, EditOrigin player) {}
    default void tick(WidgetVec cursor, EditOrigin player) {}
    default boolean interact(CodeInteraction i) {
        return false;
    }
}
