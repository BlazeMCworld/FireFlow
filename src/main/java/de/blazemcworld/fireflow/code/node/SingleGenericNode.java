package de.blazemcworld.fireflow.code.node;

import de.blazemcworld.fireflow.code.type.AllTypes;
import de.blazemcworld.fireflow.code.type.WireType;
import net.minecraft.item.Item;

import java.util.List;

public abstract class SingleGenericNode<T> extends Node {
    protected final WireType<T> type;

    protected SingleGenericNode(String id, String name, String description, Item icon, WireType<T> type) {
        super(id, name, description, icon);
        this.type = type;
    }

    @Override
    public boolean acceptsType(WireType<?> type, int index) {
        return AllTypes.isValue(type);
    }

    @Override
    public List<WireType<?>> getTypes() {
        return List.of(type);
    }

    @Override
    public int getTypeCount() {
        return 1;
    }

    @Override
    public Node copyWithTypes(List<WireType<?>> types) {
        return copyWithType(types.getFirst());
    }

    public abstract Node copyWithType(WireType<?> type);
}
