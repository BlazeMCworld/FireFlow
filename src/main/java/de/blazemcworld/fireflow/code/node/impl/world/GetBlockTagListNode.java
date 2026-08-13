package de.blazemcworld.fireflow.code.node.impl.world;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ListType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.type.VectorType;
import de.blazemcworld.fireflow.code.value.ListValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class GetBlockTagListNode extends Node {

    public GetBlockTagListNode() {
        super("get_block_tag_list", "Get Block Tag List", "Gets the list of all tag names of a block.", Items.ACACIA_STAIRS);

        Input<Vec3> position = new Input<>("position", "Position", VectorType.INSTANCE);
        Output<ListValue<String>> list = new Output<>("list", "List", ListType.of(StringType.INSTANCE));

        list.valueFrom((ctx) -> {
            Vec3 pos = position.getValue(ctx);
            BlockState blockState = ctx.evaluator.level.getBlockState(BlockPos.containing(pos));
            List<String> contents = new ArrayList<>();
            for (Property<?> property : blockState.getProperties()) {
                contents.add(property.getName());
            }
            return new ListValue<>(StringType.INSTANCE, contents);
        });
    }

    @Override
    public Node copy() {
        return new GetBlockTagListNode();
    }
}