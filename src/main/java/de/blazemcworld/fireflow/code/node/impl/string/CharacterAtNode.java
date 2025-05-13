package de.blazemcworld.fireflow.code.node.impl.string;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.StringType;
import net.minecraft.item.Items;

public class CharacterAtNode extends Node {
    public CharacterAtNode() {
        super("character_at", "Character At", "Returns the character at a specific index", Items.WHITE_WOOL);

        Input<String> string = new Input<>("string", "String", StringType.INSTANCE);
        Input<Double> index = new Input<>("index", "Index", NumberType.INSTANCE);
        Output<String> character = new Output<>("character", "Character", StringType.INSTANCE);

        character.valueFrom((ctx -> {
            int i = index.getValue(ctx).intValue();
            String s = string.getValue(ctx);
            if (i < 0 || i >= s.length()) return "";
            return String.valueOf(s.charAt(i));
        }));
    }

    @Override
    public Node copy() {
        return new CharacterAtNode();
    }
}