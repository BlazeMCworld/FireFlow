package de.blazemcworld.fireflow.code;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.blazemcworld.fireflow.code.web.WebEditor;
import de.blazemcworld.fireflow.code.widget.WidgetVec;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.WeakHashMap;

public class EditOrigin {

    private static final WeakHashMap<ServerPlayerEntity, EditOrigin> playerOrigins = new WeakHashMap<>();
    private static final WeakHashMap<WebEditor.WebUser, EditOrigin> webOrigins = new WeakHashMap<>();

    private final ServerPlayerEntity player;
    private final WebEditor.WebUser web;

    private EditOrigin(ServerPlayerEntity player, WebEditor.WebUser web) {
        this.player = player;
        this.web = web;
    }

    public static EditOrigin ofPlayer(ServerPlayerEntity player) {
        return playerOrigins.computeIfAbsent(player, p -> new EditOrigin(p, null));
    }

    public static EditOrigin ofWeb(WebEditor.WebUser web) {
        return webOrigins.computeIfAbsent(web, w -> new EditOrigin(null, w));
    }

    public @Nullable ServerPlayerEntity getPlayer() {
        return player;
    }

    public Optional<WidgetVec> getCursor(CodeEditor editor) {
        if (web == null) return Optional.empty();
        WidgetVec cursor = web.cursor;
        if (cursor == null) return Optional.empty();
        return Optional.of(new WidgetVec(editor, cursor.x(), cursor.y()));
    }

    public void sendError(String msg) {
        if (player != null) player.sendMessage(Text.literal(msg).formatted(Formatting.RED));
        if (web != null) web.sendError(msg);
    }

    public void sendInfo(String msg) {
        if (player != null) player.sendMessage(Text.literal(msg).formatted(Formatting.YELLOW));
        if (web != null) web.sendInfo(msg);
    }

    public void sendSnippet(String data) {
        if (player != null) {
            player.sendMessage(Text.literal("Snippet created! Click to copy.").setStyle(
                    Style.EMPTY.withClickEvent(new ClickEvent.CopyToClipboard(data)).withColor(TextColor.fromFormatting(Formatting.AQUA))
            ));
        }
        if (web != null) {
            JsonObject json = new JsonObject();
            json.addProperty("type", "snippet");
            json.addProperty("data", data);
            web.send(json);
        }
    }

    public boolean isWeb() {
        return web != null;
    }

    public boolean tryAuth(String webId) {
        if (!isWeb()) return false;
        return web.tryAuth(webId);
    }

    public void sendWebIfAuthorized(JsonElement json) {
        if (!isWeb()) return;
        if (web.isAuthorized()) web.send(json);
    }

    public void disconnectWeb() {
        if (!isWeb()) return;
        web.doClose();
    }

    public void tick() {
        if (web != null) {
            web.tick();
        }
    }
}
