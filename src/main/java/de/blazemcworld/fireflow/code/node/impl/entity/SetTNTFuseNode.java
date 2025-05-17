package de.blazemcworld.fireflow.code.node.impl.entity;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.EntityType;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.EntityValue;
import net.minecraft.entity.TntEntity;
import net.minecraft.item.Items;

public class SetTNTFuseNode extends Node {

    public SetTNTFuseNode() {
        super("set_tnt_fuse", "Set TNT Fuse", "Sets the fuse in ticks of the tnt entity.", Items.TNT);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<EntityValue> entity = new Input<>("entity", "Entity", EntityType.INSTANCE);
        Input<Double> fuse = new Input<>("fuse", "Fuse", NumberType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            entity.getValue(ctx).use(ctx, e -> {
                if (!(e instanceof TntEntity tnt)) return;
                tnt.setFuse(fuse.getValue(ctx).intValue());
            });
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new SetTNTFuseNode();
    }

}

