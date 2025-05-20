package de.blazemcworld.fireflow.code.node.impl.variable;

import de.blazemcworld.fireflow.code.VariableStore;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.StringType;
import net.minecraft.item.Items;

public class VariableExistsNode extends Node {

    public VariableExistsNode() {
        super(
                "variable_exists", "Variable Exists",
                "Checks if a variable with a name exists in a scope.", Items.IRON_ORE
        );

        Input<String> name = new Input<>("name", "Name", StringType.INSTANCE);
        Input<String> scope = new Input<>("scope", "Scope", StringType.INSTANCE)
                .options(VariableStore.KNOWN_SCOPES);
        Output<Boolean> exists = new Output<>("exists", "Exists", ConditionType.INSTANCE);

        exists.valueFrom((ctx) -> {
            VariableStore store = VariableStore.getScope(ctx, scope.getValue(ctx));
            if (store == null) return false;
            return store.has(name.getValue(ctx));
        });
    }

    @Override
    public Node copy() {
        return new VariableExistsNode();
    }
}

