package de.blazemcworld.fireflow.code.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.UUID;

public class PlayerType extends WireType<PlayerValue> {

    public static final PlayerType INSTANCE = new PlayerType();

    private PlayerType() {
        super("player", TextColor.fromFormatting(Formatting.GOLD), Items.PLAYER_HEAD);
    }

    @Override
    public PlayerValue defaultValue() {
        return new PlayerValue(UUID.fromString("00000000-0000-0000-0000-000000000000"));
    }

    @Override
    public PlayerValue checkType(Object obj) {
        if (obj instanceof PlayerValue player) return player;
        return null;
    }

    @Override
    public JsonElement toJson(PlayerValue obj) {
        return new JsonPrimitive(obj.uuid.toString());
    }

    @Override
    public PlayerValue fromJson(JsonElement json) {
        return new PlayerValue(UUID.fromString(json.getAsString()));
    }

    @Override
    public boolean valuesEqual(PlayerValue a, PlayerValue b) {
        return a.uuid.equals(b.uuid);
    }

    @Override
    public String getName() {
        return "Player";
    }

    @Override
    protected String stringifyInternal(PlayerValue value) {
        return value.uuid.toString();
    }
}
