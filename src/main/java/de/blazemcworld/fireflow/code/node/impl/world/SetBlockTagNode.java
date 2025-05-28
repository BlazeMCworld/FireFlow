package de.blazemcworld.fireflow.code.node.impl.world;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.SingleGenericNode;
import de.blazemcworld.fireflow.code.type.*;
import net.minecraft.block.BlockState;
import net.minecraft.item.Items;
import net.minecraft.state.property.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class SetBlockTagNode<T> extends SingleGenericNode<T> {

    @SuppressWarnings("unchecked")
    public <S extends Comparable<S>> SetBlockTagNode(WireType<T> type) {
        super("set_block_tag", type == null ? "Set Block Tag" : "Set " + type.getName() + " Block Tag", "Sets the value of a block's tag", Items.STONECUTTER, type);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<Vec3d> position = new Input<>("position", "Position", VectorType.INSTANCE);
        Input<String> tag = new Input<>("tag", "Tag", StringType.INSTANCE);
        Input<T> value = new Input<>("value", "Value", type);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            Vec3d pos = position.getValue(ctx);
            String propertyName = tag.getValue(ctx);
            T propertyValue = value.getValue(ctx);
            if (pos.x < -512 || pos.x > 511 || pos.z < -512 || pos.z > 511 || pos.y < ctx.evaluator.world.getBottomY() || pos.y > ctx.evaluator.world.getTopYInclusive()) return;

            BlockPos blockPos = BlockPos.ofFloored(pos);
            BlockState blockState = ctx.evaluator.world.getBlockState(blockPos);
            for (Property<?> property : blockState.getProperties()) {
                if (property.getName().equals(propertyName)) {
                    switch (property) {
                        case BooleanProperty booleanProperty when type == ConditionType.INSTANCE -> {
                            boolean booleanValue = (boolean) propertyValue;
                            ctx.evaluator.world.setBlockState(blockPos, blockState.with(booleanProperty, booleanValue));
                        }
                        case IntProperty intProperty when type == NumberType.INSTANCE -> {
                            int intValue = ((Double)propertyValue).intValue();
                            if (intProperty.ordinal(intValue) >= 0) {
                                ctx.evaluator.world.setBlockState(blockPos, blockState.with(intProperty, intValue));
                            }
                        }
                        case EnumProperty<?> enumProperty when type == StringType.INSTANCE -> {
                            String stringValue = (String) propertyValue;
                            Optional<S> parsedValue = ((Property<S>) property).parse(stringValue);
                            parsedValue.ifPresent(s -> ctx.evaluator.world.setBlockState(blockPos, blockState.with((Property<S>) property, s)));
                        }
                        default -> {}
                    }
                    break;
                }
            }
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new SetBlockTagNode<>(type);
    }

    @Override
    public boolean acceptsType(WireType<?> type, int index) {
        return type == ConditionType.INSTANCE || type == NumberType.INSTANCE || type == StringType.INSTANCE;
    }

    @Override
    public Node copyWithType(WireType<?> type) {
        return new SetBlockTagNode<>(type);
    }

}