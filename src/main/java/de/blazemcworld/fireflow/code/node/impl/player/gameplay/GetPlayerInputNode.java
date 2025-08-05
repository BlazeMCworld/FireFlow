package de.blazemcworld.fireflow.code.node.impl.player.gameplay;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import org.bukkit.GameMode;
import org.bukkit.Material;

public class GetPlayerInputNode extends Node {
    public GetPlayerInputNode() {
        super("get_player_input", "Get Player Input", "Gets if a player is pressing a specific key", Material.OMINOUS_TRIAL_KEY);

        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<String> key = new Input<>("key", "Key", StringType.INSTANCE)
                .options("Forward", "Backward", "Right", "Left", "Jump", "Sneak", "Sprint");
        Output<Boolean> pressed = new Output<>("pressed", "Pressed", ConditionType.INSTANCE);

        pressed.valueFrom(ctx -> player.getValue(ctx).tryGet(ctx, p -> {
            return switch(key.getValue(ctx)) {
                case "Forward" -> p.getCurrentInput().isForward();
                case "Backward" -> p.getCurrentInput().isBackward();
                case "Right" -> p.getCurrentInput().isRight();
                case "Left" -> p.getCurrentInput().isLeft();
                case "Jump" -> p.getCurrentInput().isJump();
                case "Sneak" -> p.getCurrentInput().isSneak();
                case "Sprint" -> p.getCurrentInput().isSprint();
                default -> false;
            };
        }, false));


    }

    @Override
    public Node copy() {
        return new GetPlayerInputNode();
    }
}