package de.blazemcworld.fireflow.code.node.impl.world;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.SingleGenericNode;
import de.blazemcworld.fireflow.code.type.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class SetBlockTagNode<T> extends SingleGenericNode<T> {

    @SuppressWarnings("unchecked")
    public <S extends Comparable<S>> SetBlockTagNode(WireType<T> type) {
        super("set_block_tag", type == null ? "Set Block Tag" : "Set " + type.getName() + " Block Tag", "Sets the value of a block's tag", Items.STONECUTTER, type);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<Vec3> position = new Input<>("position", "Position", VectorType.INSTANCE);
        Input<String> tag = new Input<>("tag", "Tag", StringType.INSTANCE);
        Input<T> value = new Input<>("value", "Value", type);
        Input<Boolean> sendUpdate = new Input<>("send_update", "Send Update", ConditionType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            Vec3 pos = position.getValue(ctx);
            if (pos.x < -512 || pos.x > 511 || pos.z < -512 || pos.z > 511 || pos.y < ctx.evaluator.level.getMinY() || pos.y > ctx.evaluator.level.getMaxY()) {
                ctx.sendSignal(next);
                return;
            }

            String propertyName = tag.getValue(ctx);
            T propertyValue = value.getValue(ctx);
            boolean updates = sendUpdate.getValue(ctx);
            int updateLimit = updates ? 512 : 0;
            int flags = updates ? Block.UPDATE_ALL : Block.UPDATE_SKIP_ALL_SIDEEFFECTS;

            BlockPos blockPos = BlockPos.containing(pos);
            BlockState blockState = ctx.evaluator.level.getBlockState(blockPos);
            for (Property<?> property : blockState.getProperties()) {
                if (property.getName().equals(propertyName)) {
                    switch (property) {
                        case BooleanProperty booleanProperty when type == ConditionType.INSTANCE -> {
                            ctx.evaluator.level.setBlock(blockPos, blockState.setValue(booleanProperty, (Boolean) propertyValue), flags, updateLimit);
                        }
                        case IntegerProperty integerProperty when type == NumberType.INSTANCE -> {
                            int intValue = ((Double)propertyValue).intValue();
                            if (integerProperty.getInternalIndex(intValue) >= 0) {
                                ctx.evaluator.level.setBlock(blockPos, blockState.setValue(integerProperty, intValue), flags, updateLimit);
                            }
                        }
                        case EnumProperty<?> ignored when type == StringType.INSTANCE -> {
                            String stringValue = (String) propertyValue;
                            Optional<S> parsedValue = ((Property<S>) property).getValue(stringValue);
                            parsedValue.ifPresent(s -> ctx.evaluator.level.setBlock(blockPos, blockState.setValue((Property<S>) property, s), flags, updateLimit));
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