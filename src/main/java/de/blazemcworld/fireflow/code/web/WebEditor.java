package de.blazemcworld.fireflow.code.web;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.code.CodeEditor;
import de.blazemcworld.fireflow.code.CodeInteraction;
import de.blazemcworld.fireflow.code.EditOrigin;
import de.blazemcworld.fireflow.code.widget.Widget;
import de.blazemcworld.fireflow.code.widget.WidgetVec;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceInfo;
import de.blazemcworld.fireflow.space.SpaceManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketOpen;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.server.ServerWebSocketContainer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WebEditor extends Handler.Abstract {

    private final String htmlFile;
    private final String jsFile;
    private final String cssFile;

    public WebEditor() throws IOException {
        htmlFile = readResource("page.html");
        jsFile = readResource("editor.js");
        cssFile = readResource("style.css");
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        ServerWebSocketContainer container = ServerWebSocketContainer.get(request.getContext());
        String path = Request.getPathInContext(request);
        if (path.startsWith("/ws/editor/")) {
            try {
                boolean upgraded = container.upgrade((rq, rs, cb) -> new WebUser(), request, response, callback);
                if (upgraded) return true;
            } catch (Exception e) {
                FireFlow.LOGGER.error("Failed to upgrade to websocket!", e);
            }
            Response.writeError(request, response, callback, HttpStatus.UPGRADE_REQUIRED_426);
            return true;
        }

        switch (path) {
            case "/" -> {
                response.setStatus(200);
                response.getHeaders().add("Content-Type", "text/html");
                response.write(true, ByteBuffer.wrap(htmlFile.getBytes()), callback);
                return true;
            }
            case "/editor.js" -> {
                response.setStatus(200);
                response.getHeaders().add("Content-Type", "text/javascript");
                response.write(true, ByteBuffer.wrap(jsFile.getBytes()), callback);
                return true;
            }
            case "/style.css" -> {
                response.setStatus(200);
                response.getHeaders().add("Content-Type", "text/css");
                response.write(true, ByteBuffer.wrap(cssFile.getBytes()), callback);
                return true;
            }
        }

        return false;
    }

    private String readResource(String file) throws IOException {
        return Files.readString(FabricLoader.getInstance().getModContainer("fireflow").orElseThrow().findPath("web/" + file).orElseThrow());
    }

    @WebSocket
    public static class WebUser {
        public WidgetVec cursor = null;
        private Session connection;
        private CodeEditor editor;
        private boolean authorized = false;
        private final UUID id = UUID.randomUUID();
        private final EditOrigin origin = EditOrigin.ofWeb(this);
        private final List<JsonObject> queue = new ArrayList<>();

        @OnWebSocketOpen
        public void onOpen(Session connection) {
            this.connection = connection;
        }

        @OnWebSocketClose
        public void onClose(int statusCode, String reason) {
            connection = null;
            if (authorized && editor != null) {
                editor.exitCode(origin);
            }
        }

        @OnWebSocketMessage
        public void onMessage(String payload) {
            synchronized (queue) {
                if (queue.size() > 32) return;
            }
            try {
                JsonElement json = JsonParser.parseString(payload);
                synchronized (queue) {
                    queue.add(json.getAsJsonObject());
                    if (editor == null) tick();
                }
            } catch (Exception e) {
                FireFlow.LOGGER.error("Failed to parse payload from web editor!", e);
                Session connection = this.connection;
                if (connection == null) return;
                connection.close();
            }
        }

        private void handleAuth(JsonObject json) {
            String type = json.get("type").getAsString();
            if (type.equals("init") && editor == null) {
                SpaceInfo info = SpaceManager.getInfo(json.get("id").getAsInt());
                if (info == null) {
                    sendError("Space not found!");
                    return;
                }
                Space space = SpaceManager.getIfLoaded(info);
                if (space == null) {
                    sendError("Space not currently loaded!");
                    return;
                }

                editor = space.editor;

                editor.enterCode(origin);
                for (ServerPlayerEntity player : space.getPlayers()) {
                    if (!space.info.isOwnerOrDeveloper(player.getUuid())) continue;
                    player.sendMessage(Text.literal("Someone opened the web editor for this space.").formatted(Formatting.YELLOW));
                    player.sendMessage(Text.literal("Click this to allow access, otherwise ignore it.").setStyle(
                            Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/authorize-web " + id))
                                    .withHoverEvent(new HoverEvent.ShowText(Text.literal("Warning: Only do this if you know and trust the person who opened the web editor!")
                                            .formatted(Formatting.RED)))
                                    .withFormatting(Formatting.GOLD)
                    ));
                }
                return;
            }
        }

        private void handleEditor(JsonObject json) {
            String type = json.get("type").getAsString();
            switch (type) {
                case "move-cursor" -> {
                    float x = json.get("x").getAsFloat();
                    float y = json.get("y").getAsFloat();
                    if (x > 512 || x < -512 || y > editor.world.getTopYInclusive() || y < editor.world.getBottomY()) {
                        cursor = null;
                        return;
                    }
                    cursor = new WidgetVec(null, x, y);
                    return;
                }
                case "click" -> {
                    int button = json.get("button").getAsInt();
                    if (button == 0) {
                        editor.handleInteraction(origin, CodeInteraction.Type.LEFT_CLICK, null);
                    } else if (button == 2) {
                        editor.handleInteraction(origin, CodeInteraction.Type.RIGHT_CLICK, null);
                    }
                    return;
                }
                case "key-down" -> {
                    String key = json.get("key").getAsString();
                    if (key.equals("KeyF")) {
                        editor.handleInteraction(origin, CodeInteraction.Type.SWAP_HANDS, null);
                    }
                    return;
                }
                case "chat" -> {
                    String msg = json.get("content").getAsString();

                    if (msg.startsWith("/")) {
                        handleCommand(msg);
                        return;
                    }

                    if (editor.handleInteraction(origin, CodeInteraction.Type.CHAT, msg)) return;

                    sendError("Invalid action.");
                    return;
                }
            }

        }

        private void handleCommand(String msg) {
            if (msg.equals("/reload")) {
                editor.space.reload();
                sendInfo("Reloaded space!");
                return;
            }
            if (msg.equals("/reload live")) {
                editor.space.evaluator.liveReload();
                sendInfo("Live reloaded space!");
                return;
            }

            if (msg.startsWith("/add ")) {
                editor.addNode(origin, msg.substring(5), false);
                return;
            }

            if (msg.startsWith("/add? ")) {
                editor.addNode(origin, msg.substring(6), true);
                return;
            }

            if (msg.startsWith("/function ") || msg.startsWith("/func ")) {
                msg = msg.substring(msg.indexOf(" ") + 1);
                if (msg.startsWith("create ")) {
                    editor.createFunction(origin, msg.substring(7));
                    return;
                }
                if (msg.equals("delete")) {
                    editor.deleteFunction(origin);
                    return;
                }
                if (msg.startsWith("icon ")) {
                    editor.setFunctionIcon(origin, msg.substring(5));
                    return;
                }
                if (msg.startsWith("add ")) {
                    msg = msg.substring(4);
                    if (msg.startsWith("input ")) {
                        editor.addFunctionInput(origin, msg.substring(6));
                        return;
                    }
                    if (msg.startsWith("output ")) {
                        editor.addFunctionOutput(origin, msg.substring(7));
                        return;
                    }
                    sendError("Unknown function command!");
                    return;
                }
                if (msg.startsWith("remove ")) {
                    msg = msg.substring(7);
                    if (msg.startsWith("input ")) {
                        editor.removeFunctionInput(origin, msg.substring(6));
                        return;
                    }
                    if (msg.startsWith("output ")) {
                        editor.removeFunctionOutput(origin, msg.substring(7));
                        return;
                    }
                    sendError("Unknown function command!");
                    return;
                }

                sendError("Unknown function command!");
                return;
            }

            if (msg.startsWith("/snippet ")) {
                msg = msg.substring(9);
                if (msg.equals("copy")) {
                    editor.createSnippet(origin);
                    return;
                }
                if (msg.startsWith("paste ")) {
                    editor.placeSnippet(origin, msg.substring(6).getBytes());
                    return;
                }
                sendError("Unknown snippet command!");
                return;
            }

            if (msg.startsWith("/debug ")) {
                String id = msg.substring(7);
                editor.space.evaluator.triggerDebug(id, origin);
                return;
            }

            sendError("Unknown web command!");
        }

        public void sendError(String msg) {
            JsonObject json = new JsonObject();
            json.addProperty("type", "error");
            json.addProperty("message", msg);
            send(json);
        }

        public void sendInfo(String msg) {
            JsonObject json = new JsonObject();
            json.addProperty("type", "info");
            json.addProperty("message", msg);
            send(json);
        }

        public void send(JsonElement json) {
            Session connection = this.connection;
            if (connection == null) return;
            connection.sendText(json.toString(), org.eclipse.jetty.websocket.api.Callback.NOOP);
        }

        public boolean tryAuth(String webId) {
            if (!webId.equals(id.toString()) || authorized) return false;
            authorized = true;

            for (Widget w : editor.rootWidgets) {
                w.update(); // Updates re-send all info
            }
            return true;
        }

        public void tick() {
            List<JsonObject> todo;
            synchronized (queue) {
                todo = new ArrayList<>(queue);
                queue.clear();
            }
            for (JsonObject json : todo) {
                try {
                    if (!authorized) {
                        handleAuth(json.getAsJsonObject());
                    } else {
                        editor.nextTick(() -> {
                            handleEditor(json.getAsJsonObject());
                        });
                    }
                } catch (Exception e) {
                    FireFlow.LOGGER.error("Failed to handle payload from web editor!", e);
                    Session connection = this.connection;
                    if (connection == null) return;
                    connection.close();
                }
            }
        }

        public boolean isAuthorized() {
            return authorized;
        }

        public void doClose() {
            Session connection = this.connection;
            if (connection == null) return;
            connection.close();
        }
    }
}
