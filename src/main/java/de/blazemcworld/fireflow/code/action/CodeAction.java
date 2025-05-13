package de.blazemcworld.fireflow.code.action;

import de.blazemcworld.fireflow.code.CodeEditor;
import de.blazemcworld.fireflow.code.CodeInteraction;
import de.blazemcworld.fireflow.code.widget.WidgetVec;
import net.minecraft.server.network.ServerPlayerEntity;

public interface CodeAction {
    default void stop(CodeEditor editor, ServerPlayerEntity player) {}
    default void tick(WidgetVec cursor, ServerPlayerEntity player) {}
    default boolean interact(CodeInteraction i) {
        return false;
    }
}
