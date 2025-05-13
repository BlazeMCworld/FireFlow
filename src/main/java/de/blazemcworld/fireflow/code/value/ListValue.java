package de.blazemcworld.fireflow.code.value;

import de.blazemcworld.fireflow.code.type.WireType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListValue<T> {
    
    public final WireType<T> type;
    private final List<T> store;

    public ListValue(WireType<T> type) {
        this.type = type;
        store = List.of();
    }

    public ListValue(WireType<T> type, List<T> store) {
        this.type = type;
        this.store = store;
    }

    public T get(int index) {
        if (index >= store.size() || index < 0) return type.defaultValue();
        return store.get(index);
    }

    public int size() {
        return store.size();
    }

    public ListValue<T> set(int index, T value) {
        if (index >= store.size() || index < 0) return this;
        List<T> newStore = new ArrayList<>(store);
        newStore.set(index, value);
        return new ListValue<>(type, newStore);
    }

    public ListValue<T> add(List<T> value) {
        List<T> newStore = new ArrayList<>(store);
        newStore.addAll(value);
        return new ListValue<>(type, newStore);
    }

    public ListValue<T> remove(int index) {
        if (index >= store.size() || index < 0) return this;
        List<T> newStore = new ArrayList<>(store);
        newStore.remove(index);
        return new ListValue<>(type, newStore);
    }

    public ListValue<T> insert(int index, List<T> value) {
        if (index < 0) index = 0;
        if (index > store.size()) index = store.size();
        List<T> newStore = new ArrayList<>(store);
        newStore.addAll(index, value);
        return new ListValue<>(type, newStore);
    }

    public List<T> view() {
        return Collections.unmodifiableList(store);
    }

    public ListValue<T> trim(int start, int stop) {
        if (start < 0) start = 0;
        if (stop > store.size()) stop = store.size();
        if (start > stop) return new ListValue<>(type);
        return new ListValue<>(type, new ArrayList<>(store.subList(start, stop)));
    }

    public ListValue<T> reversed() {
        return new ListValue<>(type, new ArrayList<>(store.reversed()));
    }
}
