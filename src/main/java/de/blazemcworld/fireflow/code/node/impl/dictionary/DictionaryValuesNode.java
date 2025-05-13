package de.blazemcworld.fireflow.code.node.impl.dictionary;

import de.blazemcworld.fireflow.code.node.DualGenericNode;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.DictionaryType;
import de.blazemcworld.fireflow.code.type.ListType;
import de.blazemcworld.fireflow.code.type.WireType;
import de.blazemcworld.fireflow.code.value.DictionaryValue;
import de.blazemcworld.fireflow.code.value.ListValue;
import net.minecraft.item.Items;

public class DictionaryValuesNode<K, V> extends DualGenericNode<K, V> {

    public DictionaryValuesNode(WireType<K> type1, WireType<V> type2) {
        super("dictionary_values", type1 == null || type2 == null ? "Dictionary Values" : type1.getName() + " " + type2.getName() + " Dictionary Values", "Gets the values of a dictionary.", Items.HOPPER, type1, type2);

        Input<DictionaryValue<K, V>> dict = new Input<>("dictionary", "Dictionary", DictionaryType.of(type1, type2));
        Output<ListValue<V>> values = new Output<>("values", "Values", ListType.of(type2));

        values.valueFrom((ctx) -> new ListValue<>(type2, dict.getValue(ctx).values()));
    }

    @Override
    public Node copy() {
        return new DictionaryValuesNode<>(type1, type2);
    }

    @Override
    public Node copyWithType(WireType<?> type1, WireType<?> type2) {
        return new DictionaryValuesNode<>(type1, type2);
    }
}
