package de.blazemcworld.fireflow.code.node.impl.entity;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.EntityType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.EntityValue;
import net.minecraft.world.item.Items;

public class KillEntityNode extends Node {

    public KillEntityNode() {
        super("kill_entity", "Kill Entity", "Kills the entity", Items.NETHERITE_SWORD);
        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<EntityValue> entity = new Input<>("entity", "Entity", EntityType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            entity.getValue(ctx).use(ctx, e -> e.kill(ctx.evaluator.level));
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new KillEntityNode();
    }
}
