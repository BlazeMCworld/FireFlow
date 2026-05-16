package de.blazemcworld.fireflow.code.node.impl.player.gameplay;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;
import net.minecraft.world.GameMode;

public class SetGamemodeNode extends Node {
    public SetGamemodeNode() {
        super("set_gamemode", "Set Gamemode", "Sets the gamemode of the player", Items.DIAMOND);
        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);

        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<String> gamemode = new Input<>("gamemode", "Gamemode", StringType.INSTANCE)
                .options("Creative", "Survival", "Adventure", "Spectator");
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            player.getValue(ctx).tryUse(ctx, p -> {
                GameMode mode = switch (gamemode.getValue(ctx).toLowerCase()) {
                    case "creative" -> GameMode.CREATIVE;
                    case "survival" -> GameMode.SURVIVAL;
                    case "adventure" -> GameMode.ADVENTURE;
                    case "spectator" -> GameMode.SPECTATOR;
                    default -> null;
                };
                if (mode != null) p.changeGameMode(mode);
            });
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new SetGamemodeNode();
    }
}