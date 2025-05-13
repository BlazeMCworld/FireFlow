package de.blazemcworld.fireflow.code.node.impl.text;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.TextType;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class CombineTextsNode extends Node {
    public CombineTextsNode() {
        super("combine_texts", "Combine Texts", "Combines multiple texts into one", Items.SLIME_BALL);

        Varargs<Text> texts = new Varargs<>("texts", "Texts", TextType.INSTANCE);
        Output<Text> combined = new Output<>("combined", "Combined", TextType.INSTANCE);

        combined.valueFrom(ctx -> {
            MutableText out = Text.empty();
            for (Text text : texts.getVarargs(ctx)) {
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

