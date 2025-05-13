package de.blazemcworld.fireflow.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class TextWidth {

    private static final HashMap<String, Info> known = new HashMap<>();
    private static Info missing;

    public static void init() {
        try {
            String raw = Files.readString(FabricLoader.getInstance().getModContainer("fireflow").orElseThrow().findPath("fontwidth.json").orElseThrow());

            JsonObject parsed = JsonParser.parseString(raw).getAsJsonObject();

            missing = new Gson().fromJson(parsed.get("missing_char"), Info.class);

            for (Map.Entry<String, JsonElement> entry : parsed.get("chars").getAsJsonObject().entrySet()) {
                known.put(entry.getKey(), new Gson().fromJson(entry.getValue(), Info.class));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static double calculate(Text text) {
        return calculate(text, false);
    }

    public static double calculate(Text text, boolean bold) {
        bold = text.getStyle().bold == Boolean.TRUE || (text.getStyle().bold == null && bold);

        double width = 0;
        if (text.getContent() instanceof PlainTextContent t) {
            width += calculate(t.string(), bold);
        }
        for (Text child : text.getSiblings()) {
            width += calculate(child, bold);
        }
        return width;
    }

    public static double calculate(String text, boolean bold) {
        double width = 0;

        for (String s : text.split("")) {
            Info info = known.getOrDefault(s, missing);
            width += info.width;
            if (bold) width += info.bold;
        }

        return width;
    }

    private record Info(double width, double bold) {}
}
