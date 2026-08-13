package de.blazemcworld.fireflow.code.node.impl.text;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.TextType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

public class CombineTextsNode extends Node {
    public CombineTextsNode() {
        super("combine_texts", "Combine Texts", "Combines multiple texts into one", Items.SLIME_BALL);

        Varargs<Component> texts = new Varargs<>("texts", "Texts", TextType.INSTANCE);
        Output<Component> combined = new Output<>("combined", "Combined", TextType.INSTANCE);

        combined.valueFrom(ctx -> {
            MutableComponent out = Component.empty();
            for (Component text : texts.getVarargs(ctx)) {
                out.append(text);
            }
            return out;
        });
    }

    @Override
    public Node copy() {
        return new CombineTextsNode();
    }
}

