package de.blazemcworld.fireflow.code.node.impl.dictionary;

import de.blazemcworld.fireflow.code.node.DualGenericNode;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.DictionaryType;
import de.blazemcworld.fireflow.code.type.WireType;
import de.blazemcworld.fireflow.code.value.DictionaryValue;
import net.minecraft.item.Items;

public class DictionaryRemoveNode<K, V> extends DualGenericNode<K, V> {

    public DictionaryRemoveNode(WireType<K> type1, WireType<V> type2) {
        super("dictionary_remove", type1 == null || type2 == null ? "Dictionary Remove" : type1.getName() + " " + type2.getName() + " Dictionary Remove", "Removes a key from a dictionary.", Items.TNT, type1, type2);

        Input<DictionaryValue<K, V>> dict = new Input<>("dictionary", "Dictionary", DictionaryType.of(type1, type2));
        Input<K> key = new Input<>("key", "Key", type1);

        Output<DictionaryValue<K, V>> updated = new Output<>("updated", "Updated", DictionaryType.of(type1, type2));

        updated.valueFrom((ctx) -> dict.getValue(ctx).remove(key.getValue(ctx)));
    }

    @Override
    public Node copy() {
        return new DictionaryRemoveNode<>(type1, type2);
    }

    @Override
    public Node copyWithType(WireType<?> type1, WireType<?> type2) {
        return new DictionaryRemoveNode<>(type1, type2);
    }
}

