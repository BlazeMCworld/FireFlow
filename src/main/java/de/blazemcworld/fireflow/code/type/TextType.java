package de.blazemcworld.fireflow.code.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import de.blazemcworld.fireflow.FireFlow;
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.world.item.Items;

public class TextType extends WireType<Component> {

    public static final TextType INSTANCE = new TextType();

    public static final MiniMessage MM = MiniMessage.builder()
            .tags(TagResolver.builder().resolvers(
                    StandardTags.color(),
                    StandardTags.decorations(),
                    StandardTags.font(),
                    StandardTags.gradient(),
                    StandardTags.keybind(),
                    StandardTags.newline(),
                    StandardTags.rainbow(),
                    StandardTags.reset(),
                    StandardTags.transition(),
                    StandardTags.translatable(),
                    StandardTags.hoverEvent()
            ).build()).build();

    private TextType() {
        super("text", TextColor.LIGHT_PURPLE, Items.BOOK);
    }

    @Override
    public String getName() {
        return "Text";
    }

    @Override
    public Component defaultValue() {
        return Component.empty();
    }

    @Override
    public Component parseInset(String str) {
        return MinecraftServerAudiences.of(FireFlow.server).asNative(MM.deserialize(str));
    }

    @Override
    protected String stringifyInternal(Component value, String mode) {
        return switch (mode) {
            case "plain" -> getPlainContent(value);
            default -> MM.serialize(MinecraftServerAudiences.of(FireFlow.server).asAdventure(value));
        };
    }

    private static String getPlainContent(Component text) {
        StringBuilder out = new StringBuilder();
        if (text.getContents() instanceof PlainTextContents.LiteralContents(String literal)) {
            out.append(literal);
        }
        for (Component child : text.getSiblings()) {
            out.append(getPlainContent(child));
        }
        return out.toString();
    }

    @Override
    public Component checkType(Object obj) {
        if (obj instanceof Component comp) return comp;
        return null;
    }

    @Override
    public JsonElement toJson(Component obj) {
        return new JsonPrimitive(MM.serialize(MinecraftServerAudiences.of(FireFlow.server).asAdventure(obj)));
    }

    @Override
    public Component fromJson(JsonElement json) {
        return MinecraftServerAudiences.of(FireFlow.server).asNative(MM.deserialize(json.getAsString()));
    }

    @Override
    public boolean valuesEqual(Component a, Component b) {
        return stringifyInternal(a, "display").equals(stringifyInternal(b, "display"));
    }

    @Override
    protected boolean canConvertInternal(WireType<?> other) {
        return AllTypes.isValue(other);
    }

    @Override
    protected Component convertInternal(WireType<?> other, Object v) {
        return Component.literal(other.stringify(v, "display"));
    }
}
