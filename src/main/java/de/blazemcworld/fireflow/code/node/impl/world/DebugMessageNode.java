package de.blazemcworld.fireflow.code.node.impl.world;

import com.google.gson.JsonObject;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.StringType;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class DebugMessageNode extends Node {

    public DebugMessageNode() {
        super("debug_message", "Debug Message", "Sends a message to all developers on the space.", Items.NETHER_STAR);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<Boolean> disable = new Input<>("disable", "Disable", ConditionType.INSTANCE);
        Varargs<String> info = new Varargs<>("info", "Info", StringType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            if (disable.getValue(ctx)) {
                ctx.sendSignal(next);
                return;
            }

            StringBuilder out = new StringBuilder();
            for (String msg : info.getVarargs(ctx)) {
                out.append(msg).append(" ");
            }
            out.setLength(out.length() - 1);

            Text msg = Text.literal("Debug: ").formatted(Formatting.AQUA).append(Text.literal(out.toString()).formatted(Formatting.DARK_AQUA));
            for (ServerPlayerEntity player : ctx.evaluator.space.getPlayers()) {
                if (ctx.evaluator.space.info.isOwnerOrDeveloper(player.getUuid())) {
                    player.sendMessage(msg, false);
                }
            }
            JsonObject json = new JsonObject();
            json.addProperty("type", "info");
            json.addProperty("message", "Debug: " + out);
            ctx.evaluator.space.editor.webBroadcast(json);
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new DebugMessageNode();
    }
}
