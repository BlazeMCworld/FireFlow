package de.blazemcworld.fireflow.code.node.impl.dictionary;

import de.blazemcworld.fireflow.code.node.DualGenericNode;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.DictionaryType;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.WireType;
import de.blazemcworld.fireflow.code.value.DictionaryValue;
import net.minecraft.item.Items;

public class DictionarySizeNode<K, V> extends DualGenericNode<K, V> {

    public DictionarySizeNode(WireType<K> type1, WireType<V> type2) {
        super("dictionary_size", type1 == null || type2 == null ? "Dictionary Size" : type1.getName() + " " + type2.getName() + " Dictionary Size", "Gets the size of a dictionary.", Items.KNOWLEDGE_BOOK, type1, type2);

        Input<DictionaryValue<K, V>> dict = new Input<>("dictionary", "Dictionary", DictionaryType.of(type1, type2));
        Output<Double> size = new Output<>("size", "Size", NumberType.INSTANCE);

        size.valueFrom((ctx) -> (double) dict.getValue(ctx).size());
    }

    @Override
    public Node copy() {
        return new DictionarySizeNode<>(type1, type2);
    }

    @Override
    public Node copyWithType(WireType<?> type1, WireType<?> type2) {
        return new DictionarySizeNode<>(type1, type2);
    }
}


