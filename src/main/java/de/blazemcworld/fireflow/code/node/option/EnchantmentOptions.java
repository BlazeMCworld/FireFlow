package de.blazemcworld.fireflow.code.node.option;

import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.code.CodeInteraction;
import de.blazemcworld.fireflow.code.widget.ChoiceWidget;
import de.blazemcworld.fireflow.code.widget.NodeIOWidget;
import de.blazemcworld.fireflow.code.widget.WidgetVec;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EnchantmentOptions implements InputOptions {

    public static final EnchantmentOptions INSTANCE = new EnchantmentOptions();
    private final List<String> effects = new ArrayList<>();

    private EnchantmentOptions() {
        for (Identifier id : FireFlow.server.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getIds()) {
            effects.add(id.getPath());
        }
        effects.sort(String::compareTo);
    }

    @Override
    public boolean handleRightClick(Consumer<String> update, NodeIOWidget io, CodeInteraction i) {
        WidgetVec pos = io.pos().add(2.5, 0);
        ChoiceWidget w = new ChoiceWidget(pos, effects, update);
        pos.editor().rootWidgets.add(w);
        w.update();
        return true;
    }

    @Override
    public String fallback() {
        return "sharpness";
    }
}
