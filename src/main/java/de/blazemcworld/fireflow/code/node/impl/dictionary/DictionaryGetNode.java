package de.blazemcworld.fireflow.code.node.impl.dictionary;

import de.blazemcworld.fireflow.code.node.DualGenericNode;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.DictionaryType;
import de.blazemcworld.fireflow.code.type.WireType;
import de.blazemcworld.fireflow.code.value.DictionaryValue;
import net.minecraft.item.Items;

public class DictionaryGetNode<K, V> extends DualGenericNode<K, V> {

    public DictionaryGetNode(WireType<K> type1, WireType<V> type2) {
        super("dictionary_get", type1 == null || type2 == null ? "Dictionary Get" : type1.getName() + " " + type2.getName() + " Dictionary Get", "Gets a value from a dictionary.", Items.HOPPER, type1, type2);

        Input<DictionaryValue<K, V>> dict = new Input<>("dictionary", "Dictionary", DictionaryType.of(type1, type2));
        Input<K> key = new Input<>("key", "Key", type1);

        Output<V> value = new Output<>("value", "Value", type2);

        value.valueFrom((ctx) -> dict.getValue(ctx).get(key.getValue(ctx)));
    }

    @Override
    public Node copy() {
        return new DictionaryGetNode<>(type1, type2);
    }

    @Override
    public Node copyWithType(WireType<?> type1, WireType<?> type2) {
        return new DictionaryGetNode<>(type1, type2);
    }
}

