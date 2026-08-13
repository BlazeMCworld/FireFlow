package de.blazemcworld.fireflow.code.node.impl.player.meta;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;

public class PlayerFromNameNode extends Node {
    public PlayerFromNameNode() {
        super("player_from_name", "Player From Name", "Gets an online player based on their name", Items.PLAYER_HEAD);

        Input<String> name = new Input<>("name", "Name", StringType.INSTANCE);
        Output<PlayerValue> player = new Output<>("player", "Player", PlayerType.INSTANCE);

        player.valueFrom(ctx -> {
            for (ServerPlayer p : ctx.evaluator.players()) {
                if (p.getGameProfile().name().equalsIgnoreCase(name.getValue(ctx))) {
                    return new PlayerValue(p);
                }
            }
            return PlayerType.INSTANCE.defaultValue();
        });
    }

    @Override
    public Node copy() {
        return new PlayerFromNameNode();
    }
}