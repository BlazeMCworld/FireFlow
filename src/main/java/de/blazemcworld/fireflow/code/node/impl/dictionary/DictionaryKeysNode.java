package de.blazemcworld.fireflow.code.node.impl.dictionary;

import de.blazemcworld.fireflow.code.node.DualGenericNode;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.DictionaryType;
import de.blazemcworld.fireflow.code.type.ListType;
import de.blazemcworld.fireflow.code.type.WireType;
import de.blazemcworld.fireflow.code.value.DictionaryValue;
import de.blazemcworld.fireflow.code.value.ListValue;
import net.minecraft.item.Items;

public class DictionaryKeysNode<K, V> extends DualGenericNode<K, V> {

    public DictionaryKeysNode(WireType<K> type1, WireType<V> type2) {
        super("dictionary_keys", type1 == null || type2 == null ? "Dictionary Keys" : type1.getName() + " " + type2.getName() + " Dictionary Keys", "Gets the keys of a dictionary.", Items.TRIAL_KEY, type1, type2);

        Input<DictionaryValue<K, V>> dict = new Input<>("dictionary", "Dictionary", DictionaryType.of(type1, type2));
        Output<ListValue<K>> keys = new Output<>("keys", "Keys", ListType.of(type1));

        keys.valueFrom((ctx) -> new ListValue<>(type1, dict.getValue(ctx).keys()));
    }

    @Override
    public Node copy() {
        return new DictionaryKeysNode<>(type1, type2);
    }

    @Override
    public Node copyWithType(WireType<?> type1, WireType<?> type2) {
        return new DictionaryKeysNode<>(type1, type2);
    }
}


