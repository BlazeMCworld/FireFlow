package de.blazemcworld.fireflow.code.node.impl.world;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.SingleGenericNode;
import de.blazemcworld.fireflow.code.type.*;
import de.blazemcworld.fireflow.code.value.ListValue;
import net.minecraft.block.BlockState;
import net.minecraft.item.Items;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class GetBlockTagInfoNode<T> extends SingleGenericNode<T> {

    public GetBlockTagInfoNode(WireType<T> type) {
        super("get_block_tag_info", type == null ? "Get Block Tag Info" : "Get " + type.getName() + " Block Tag Info", "Gets the current value and all valid options of a block's tag", Items.OAK_STAIRS, type);

        Input<Vec3d> position = new Input<>("position", "Position", VectorType.INSTANCE);
        Input<String> property = new Input<>("property", "Property", StringType.INSTANCE);
        Output<T> value = new Output<>("value", "Value", type);
        Output<ListValue<T>> allOptions = new Output<>("all_options", "All Options", ListType.of(type));

        value.valueFrom((ctx) -> {
            String propertyName = property.getValue(ctx);
            Vec3d pos = position.getValue(ctx);
            BlockState blockState = ctx.evaluator.world.getBlockState(BlockPos.ofFloored(pos));
            for (Property<?> prop : blockState.getProperties()) {
                if (prop.getName().equals(propertyName)) {
                    Comparable<?> propertyValue = blockState.get(prop);
                    switch (propertyValue) {
                        case Integer intValue -> {
                            return type.convert(NumberType.INSTANCE, intValue.doubleValue());
                        }
                        case StringIdentifiable enumValue -> {
                            String stringValue = enumValue.asString();
                            return type.convert(StringType.INSTANCE, stringValue);
                        }
                        case Boolean boolValue -> {
                            return type.convert(ConditionType.INSTANCE, boolValue);
                        }
                        default -> {}
                    }
                    break;
                }
            }
            return type.defaultValue();
        });

        allOptions.valueFrom((ctx) -> {
            String propertyName = property.getValue(ctx);
            Vec3d pos = position.getValue(ctx);
            BlockState blockState = ctx.evaluator.world.getBlockState(BlockPos.ofFloored(pos));
            for (Property<?> prop : blockState.getProperties()) {
                if (prop.getName().equals(propertyName)) {
                    switch (prop) {
                        case IntProperty intProperty -> {
                            List<T> list = new ArrayList<>();
                            for (Integer intValue : intProperty.getValues()) {
                                list.add(type.convert(NumberType.INSTANCE, Double.valueOf(intValue)));
                            }
                            return new ListValue<>(type, list);
                        }
                        case EnumProperty<?> enumProperty -> {
                            List<T> list = new ArrayList<>();
                            for (StringIdentifiable stringIdentifiable : enumProperty.getValues()) {
                                list.add(type.convert(StringType.INSTANCE, stringIdentifiable.asString()));
                            }
                            return new ListValue<>(type, list);
                        }
                        case BooleanProperty booleanProperty -> {
                            List<T> list = new ArrayList<>();
                            for (boolean boolValue : booleanProperty.getValues()) {
                                list.add(type.convert(ConditionType.INSTANCE, boolValue));
                            }
                            return new ListValue<>(type, list);
                        }
                        default -> {}
                    }
                    break;
                }
            }
            return allOptions.type.defaultValue();
        });
    }

    @Override
    public Node copy() {
        return new GetBlockTagInfoNode<>(type);
    }

    @Override
    public boolean acceptsType(WireType<?> type, int index) {
        return type == ConditionType.INSTANCE || type == NumberType.INSTANCE || type == StringType.INSTANCE;
    }

    @Override
    public Node copyWithType(WireType<?> type) {
        return new GetBlockTagInfoNode<>(type);
    }

}