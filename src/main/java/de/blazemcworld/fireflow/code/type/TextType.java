package de.blazemcworld.fireflow.code.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import de.blazemcworld.fireflow.FireFlow;
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.minecraft.item.Items;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

public class TextType extends WireType<Text> {

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
        super("text", TextColor.fromFormatting(Formatting.LIGHT_PURPLE), Items.BOOK);
    }

    @Override
    public String getName() {
        return "Text";
    }

    @Override
    public Text defaultValue() {
        return Text.empty();
    }

    @Override
    public Text parseInset(String str) {
        return MinecraftServerAudiences.of(FireFlow.server).asNative(MM.deserialize(str));
    }

    @Override
    protected String stringifyInternal(Text value, String mode) {
        return switch (mode) {
            case "plain" -> getPlainContent(value);
            default -> MM.serialize(MinecraftServerAudiences.of(FireFlow.server).asAdventure(value));
        };
    }

    private static String getPlainContent(Text text) {
        StringBuilder out = new StringBuilder();
        if (text.getContent() instanceof PlainTextContent.Literal(String literal)) {
            out.append(literal);
        }
        for (Text child : text.getSiblings()) {
            out.append(getPlainContent(child));
        }
        return out.toString();
    }

    @Override
    public Text checkType(Object obj) {
        if (obj instanceof Text comp) return comp;
        return null;
    }

    @Override
    public JsonElement toJson(Text obj) {
        return new JsonPrimitive(MM.serialize(MinecraftServerAudiences.of(FireFlow.server).asAdventure(obj)));
    }

    @Override
    public Text fromJson(JsonElement json) {
        return MinecraftServerAudiences.of(FireFlow.server).asNative(MM.deserialize(json.getAsString()));
    }

    @Override
    public boolean valuesEqual(Text a, Text b) {
        return stringifyInternal(a, "display").equals(stringifyInternal(b, "display"));
    }

    @Override
    protected boolean canConvertInternal(WireType<?> other) {
        return AllTypes.isValue(other);
    }

    @Override
    protected Text convertInternal(WireType<?> other, Object v) {
        return Text.literal(other.stringify(v, "display"));
    }
}
