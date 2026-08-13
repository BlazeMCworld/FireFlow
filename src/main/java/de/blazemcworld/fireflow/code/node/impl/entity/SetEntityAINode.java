package de.blazemcworld.fireflow.code.node.impl.entity;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.EntityType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.EntityValue;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Items;

public class SetEntityAINode extends Node {

    public SetEntityAINode() {
        super("set_entity_ai", "Set Entity AI", "Changes whether a mob will move on its own.", Items.BRAIN_CORAL);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<EntityValue> entity = new Input<>("entity", "Entity", EntityType.INSTANCE);
        Input<Boolean> enable = new Input<>("enable", "Enable", ConditionType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            entity.getValue(ctx).use(ctx, e -> {
                if (!(e instanceof Mob m)) return;
                m.setNoAi(!enable.getValue(ctx));
            });
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new SetEntityAINode();
    }
}
