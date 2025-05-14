package de.blazemcworld.fireflow.code.node.option;

import de.blazemcworld.fireflow.code.CodeInteraction;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.widget.NodeIOWidget;

import java.util.List;
import java.util.function.Consumer;

public interface InputOptions {
    String fallback();

    default boolean handleLeftClick(Consumer<String> update, NodeIOWidget io, CodeInteraction i) {
        return false;
    }

    default boolean handleRightClick(Consumer<String> update, NodeIOWidget io, CodeInteraction i) {
        return false;
    }

    record Choice(List<String> list, Node.Input<?> input) implements InputOptions {
        @Override
        public String fallback() {
            return list.getFirst();
        }

        @Override
        public boolean handleLeftClick(Consumer<String> update, NodeIOWidget io, CodeInteraction i) {
            int currentOption = list.indexOf(input.inset);
            int next = currentOption + 1;
            if (next >= list.size()) next = 0;
            update.accept(list.get(next));
            return true;
        }

        @Override
        public boolean handleRightClick(Consumer<String> update, NodeIOWidget io, CodeInteraction i) {
            int currentOption = list.indexOf(input.inset);
            int next = currentOption - 1;
            if (next < 0) next = list.size() - 1;
            update.accept(list.get(next));
            return true;
        }
    }
}
