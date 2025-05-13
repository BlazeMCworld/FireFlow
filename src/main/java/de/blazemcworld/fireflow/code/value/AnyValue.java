package de.blazemcworld.fireflow.code.value;

import de.blazemcworld.fireflow.code.type.WireType;

public record AnyValue<T>(T value, WireType<T> type) {
}
