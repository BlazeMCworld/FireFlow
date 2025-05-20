package de.blazemcworld.fireflow.code.node.impl.variable;

import de.blazemcworld.fireflow.code.VariableStore;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.StringType;
import net.minecraft.item.Items;

public class RemoveVariableNode extends Node {

    public RemoveVariableNode() {
        super(
                "remove_variable", "Remove Variable",
                "Removes a variable from a scope.", Items.TNT
        );

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<String> name = new Input<>("name", "Name", StringType.INSTANCE);
        Input<String> scope = new Input<>("scope", "Scope", StringType.INSTANCE)
                .options(VariableStore.KNOWN_SCOPES);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            VariableStore store = VariableStore.getScope(ctx, scope.getValue(ctx));
            if (store != null) store.remove(name.getValue(ctx));
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new RemoveVariableNode();
    }

}

