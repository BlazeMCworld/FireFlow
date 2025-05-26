package de.blazemcworld.fireflow.code.node.impl.player.meta;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ListType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.value.ListValue;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class PlayerListNode extends Node {

    public PlayerListNode() {
        super("player_list", "Player List", "Gets a list of all players currently playing on the space", Items.LIGHT_BLUE_DYE);

        Output<ListValue<PlayerValue>> playing = new Output<>("players", "Players", ListType.of(PlayerType.INSTANCE));

        playing.valueFrom((ctx) ->{
            List<PlayerValue> out = new ArrayList<>();
            for (ServerPlayerEntity player : ctx.evaluator.world.getPlayers()) {
                if (ModeManager.getFor(player) != ModeManager.Mode.PLAY) continue;
                out.add(new PlayerValue(player));
            }
            return new ListValue<>(PlayerType.INSTANCE, out);
        });
    }

    @Override
    public Node copy() {
        return new PlayerListNode();
    }
}
