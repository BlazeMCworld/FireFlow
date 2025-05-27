package de.blazemcworld.fireflow.messages;

import de.blazemcworld.fireflow.FireFlow;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Messages {

    private Messages() {

    }

    /**
     * Used for messages conveying information that do not fit into other categories.
     */
    public static final Type INFO = new SimpleMessageType(
            ColourPalette.LIGHT_GREY_2.rgb24,
            ColourPalette.CORNFLOWER_PASTEL.rgb24,
            "ℹ"
    );

    /**
     * Used to notify the user of a successful action.
     */
    public static final Type SUCCESS = new SimpleMessageType(
            ColourPalette.LIGHT_GREY_2.rgb24,
            ColourPalette.MINT_PASTEL.rgb24,
            "✔"
    );

    /**
     * Used to notify the user of an error.
     */
    public static final Type ERROR = new SoundingMessageType(
            ColourPalette.ROSE_PASTEL_2.rgb24,
            ColourPalette.ROSE_DARK.rgb24,
            "❌",
            Sound.sound()
                    .type(Key.key("item.mace.smash_air"))
                    .volume(0.25f)
                    .source(Sound.Source.MASTER)
                    .build()
    );

    /**
     * Used to provide additional context to previous messages.
     */
    public static final Type FOLLOWUP = new SimpleMessageType(
            ColourPalette.LIGHT_GREY_2.rgb24,
            ColourPalette.LIME_PASTEL.rgb24,
            "→"
    );


    /**
     * Escapes minimessage tags in the given message
     * @param input The message
     * @param type The type, if null use default minimessage, this may miss some tags
     * @return The escaped message
     */
    public static String escapeMiniMessage(@NotNull String input, @Nullable Type type) {
        MiniMessage mm = type == null ? MiniMessage.miniMessage() : type.buildMiniMessage(MiniMessage.builder());
        return mm.escapeTags(input);
    }

    /**
     * Styles a message as the given type. Most types provide a &lt;default&gt; tag that sets the colour to the default.
     * @param message The message, MiniMessage.
     * @param type The type to style as.
     * @return The styled message.
     */
    public static Text styleMessage(@NotNull String message, @NotNull Type type) {
        MiniMessage mm = type.buildMiniMessage(MiniMessage.builder());
        String tag = type.providesDefaultTag() ? "<default>" : "";
        Text deserialized = MinecraftServerAudiences.of(FireFlow.server).asNative(mm.deserialize(tag+message));
        return type.addDecorations(deserialized);
    }

    /**
     * Sends a message of the given type to a given audience. Most types provide a &lt;default&gt; tag that sets the colour to the default.
     * @param message The message, MiniMessage.
     * @param type The type to style as.
     * @param audience The audience to send to.
     */
    public static void sendMessage(@NotNull String message, @NotNull Type type, @NotNull Audience audience) {
        Text styled = styleMessage(message, type);
        audience.sendMessage(MinecraftServerAudiences.of(FireFlow.server).asAdventure(styled));
        type.audienceEffects(audience);
    }



    public interface Type {

        Text addDecorations(Text text);

        int getColour();

        default MiniMessage buildMiniMessage(MiniMessage.Builder builder) {
            return builder.tags(
                    TagResolver.builder()
                            .resolver(TagResolver.standard())
                            .tag("default", Tag.styling(style -> style.color(TextColor.color(getColour()))))
                            .build()
            ).build();
        }

        default boolean providesDefaultTag() {
            return true;
        }

        default void audienceEffects(Audience audience) {

        }

    }

    private static class SimpleMessageType implements Type {

        private final int defaultColour;
        private final MutableText cachedIcon;

        public SimpleMessageType(int defaultColour, int iconColour, String icon) {
            this.defaultColour = defaultColour;
            this.cachedIcon = Text.literal(icon+" ").withColor(iconColour);
        }

        @Override
        public Text addDecorations(Text text) {
            return cachedIcon.copy().append(text);
        }

        @Override
        public int getColour() {
            return defaultColour;
        }

    }

    private static class SoundingMessageType extends SimpleMessageType {

        private final Sound sound;

        public SoundingMessageType(int defaultColour, int iconColour, String icon, Sound sound) {
            super(defaultColour, iconColour, icon);
            this.sound = sound;
        }

        @Override
        public void audienceEffects(Audience audience) {
            super.audienceEffects(audience);
            audience.playSound(sound);
        }
    }

}
