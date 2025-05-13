package de.blazemcworld.fireflow.code.node.impl.variable;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.SingleGenericNode;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.WireType;
import net.minecraft.item.Items;

public class CacheValueNode<T> extends SingleGenericNode<T> {

    public CacheValueNode(WireType<T> type) {
        super("cache_value", "Cache Value", "Stores a value for the active thread.", Items.KNOWLEDGE_BOOK, type);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<T> store = new Input<>("store", "Store", type);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);
        Output<T> cache = new Output<>("cache", "Cache", type);

        cache.valueFromScope();
        signal.onSignal((ctx) -> {
            ctx.setScopeValue(cache, store.getValue(ctx));
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new CacheValueNode<>(type);
    }

    @Override
    public Node copyWithType(WireType<?> type) {
        return new CacheValueNode<>(type);
    }
}
