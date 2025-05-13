package de.blazemcworld.fireflow.code.node.impl.variable;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.SingleGenericNode;
import de.blazemcworld.fireflow.code.type.WireType;
import net.minecraft.item.Items;

import java.util.List;

public class RandomChoiceNode<T> extends SingleGenericNode<T> {

    public RandomChoiceNode(WireType<T> type) {
        super("random_choice", type == null ? "Random Choice" : "Random " + type.getName() + " Choice", "Chooses a random value from the given options.", Items.TRIAL_KEY, type);

        Varargs<T> options = new Varargs<>("options", "Options", type);
        Output<T> chosen = new Output<>("chosen", "Chosen", type);

        chosen.valueFrom((ctx) -> {
            List<T> opt = options.getVarargs(ctx);
            if (opt.isEmpty()) return type.defaultValue();
            return opt.get(ctx.evaluator.world.random.nextInt(opt.size()));
        });
    }

    @Override
    public Node copy() {
        return new RandomChoiceNode<>(type);
    }

    @Override
    public Node copyWithType(WireType<?> type) {
        return new RandomChoiceNode<>(type);
    }
}
