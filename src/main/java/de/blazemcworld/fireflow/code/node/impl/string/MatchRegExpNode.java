package de.blazemcworld.fireflow.code.node.impl.string;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ListType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.value.ListValue;
import net.minecraft.item.Items;

public class MatchRegExpNode extends Node {
    public MatchRegExpNode() {
        super("match_regexp", "Match RegExp", "Match a regular expression against a string.", Items.TRIPWIRE_HOOK);

        Input<String> inputString = new Input<>("inputString", "Input String", StringType.INSTANCE);
        Input<String> exprString = new Input<>("exprString", "Expression", StringType.INSTANCE);
        Output<ListValue<String>> result = new Output<>("result", "Result", ListType.of(StringType.INSTANCE));

        result.valueFrom(ctx -> new ListValue<>(StringType.INSTANCE, Pattern.compile(exprString.getValue(ctx))
                .matcher(inputString.getValue(ctx))
                .results().map(MatchResult::group)
                .toList()));
    }

    @Override
    public Node copy() {
        return new MatchRegExpNode();
    }
}
