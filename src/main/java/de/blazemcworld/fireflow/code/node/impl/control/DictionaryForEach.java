package de.blazemcworld.fireflow.code.node.impl.control;

import de.blazemcworld.fireflow.code.node.DualGenericNode;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.DictionaryType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.WireType;
import de.blazemcworld.fireflow.code.value.DictionaryValue;
import net.minecraft.item.Items;

import java.util.List;

public class DictionaryForEach<K, V> extends DualGenericNode<K, V> {

    public DictionaryForEach(WireType<K> type1, WireType<V> type2) {
        super("dictionary_for_each", type1 == null || type2 == null ? "Dictionary For Each" : type1.getName() + " " + type2.getName() + " Dictionary For Each", "Iterates over all entries in a dictionary.", Items.HOPPER_MINECART, type1, type2);
        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<DictionaryValue<K, V>> dict = new Input<>("dictionary", "Dictionary", DictionaryType.of(type1, type2));

        Output<Void> each = new Output<>("each", "Each", SignalType.INSTANCE);
        Output<K> key = new Output<>("key", "Key", type1);
        Output<V> value = new Output<>("value", "Value", type2);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);
        key.valueFromScope();
        value.valueFromScope();

        signal.onSignal((ctx) -> {
            DictionaryValue<K, V> d = dict.getValue(ctx);
            List<K> keys = d.keys();
            int[] index = new int[] { 0 };

            new Runnable() {
                @Override
                public void run() {
                    if (index[0] >= keys.size()) {
                        ctx.sendSignal(next);
                        return;
                    }
                    ctx.setScopeValue(key, keys.get(index[0]++));
                    ctx.setScopeValue(value, d.get(keys.get(index[0] - 1)));
                    ctx.submit(this);
                    ctx.sendSignal(each);
                }
            }.run();
        });
    }

    @Override
    public Node copy() {
        return new DictionaryForEach<>(type1, type2);
    }

    @Override
    public Node copyWithType(WireType<?> keyType, WireType<?> valueType) {
        return new DictionaryForEach<>(keyType, valueType);
    }
}
