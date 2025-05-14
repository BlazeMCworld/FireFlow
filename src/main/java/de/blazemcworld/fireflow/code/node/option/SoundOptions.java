package de.blazemcworld.fireflow.code.node.option;

import de.blazemcworld.fireflow.code.CodeInteraction;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.widget.ChoiceWidget;
import de.blazemcworld.fireflow.code.widget.NodeIOWidget;
import de.blazemcworld.fireflow.code.widget.WidgetVec;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public record SoundOptions(Node.Input<String> input) implements InputOptions {

    private static final List<String> soundIds = new ArrayList<>();

    static {
        for (Identifier id : Registries.SOUND_EVENT.getIds()) {
            soundIds.add(id.getPath());
        }
        soundIds.remove("intentionally_empty");
    }

    @Override
    public String fallback() {
        return "block.note_block.pling";
    }

    @Override
    public boolean handleRightClick(Consumer<String> update, NodeIOWidget io, CodeInteraction i) {
        openSelector(io.pos().add(2.5, 0), update, "back_if_empty");
        return true;
    }

    private void openSelector(WidgetVec pos, Consumer<String> update, String mode) {
        Set<String> choices = new HashSet<>();
        String current = input.inset;
        for (String sound : soundIds) {
            if (!sound.startsWith(current)) continue;
            String part = sound.substring(current.length());
            if (part.startsWith(".")) continue;
            int nextDot = part.indexOf('.');
            if (nextDot != -1) {
                part = part.substring(0, nextDot + 1);
            }
            if (part.isEmpty()) continue;
            choices.add(part);
        }

        if (choices.isEmpty()) {
            if (mode.equals("hide_if_empty")) return;
            if (mode.equals("back_if_empty")) {
                String newValue = current.substring(0, current.length() - 1);
                int lastDot = newValue.lastIndexOf('.');
                if (lastDot != -1) {
                    update.accept(newValue.substring(0, lastDot + 1));
                } else {
                    update.accept("");
                }
                openSelector(pos, update, "hide_if_empty");
                return;
            }
        }

        List<String> list = new ArrayList<>(choices);
        list.sort(String::compareTo);

        if (!current.isEmpty()) {
            list.addFirst("🠄 Back");
        }

        ChoiceWidget w = new ChoiceWidget(pos, list, (choice) -> {
            String newValue = current + choice;
            if (choice.equals("🠄 Back")) {
                newValue = current.substring(0, current.length() - 1);
                int lastDot = newValue.lastIndexOf('.');
                if (lastDot != -1) {
                    newValue = newValue.substring(0, lastDot + 1);
                } else {
                    newValue = "";
                }
            }
            update.accept(newValue);
            openSelector(pos, update, "hide_if_empty");
        });
        pos.editor().rootWidgets.add(w);
        w.update();
    }
}
