package de.blazemcworld.fireflow.code;

import de.blazemcworld.fireflow.code.widget.WidgetVec;

public record CodeInteraction(EditOrigin origin, WidgetVec pos, Type type, String message) {
    public enum Type {
        LEFT_CLICK, RIGHT_CLICK, SWAP_HANDS, CHAT
    }
}
