package de.blazemcworld.fireflow.code.node.impl.world;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ListType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.type.VectorType;
import de.blazemcworld.fireflow.code.value.ListValue;
import net.minecraft.block.BlockState;
import net.minecraft.item.Items;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class GetBlockTagListNode extends Node {

    public GetBlockTagListNode() {
        super("get_block_tag_list", "Get Block Tag List", "Gets the list of all tag names of a block.", Items.OAK_STAIRS);

        Input<Vec3d> position = new Input<>("position", "Position", VectorType.INSTANCE);
        Output<ListValue<String>> list = new Output<>("list", "List", ListType.of(StringType.INSTANCE));

        list.valueFrom((ctx) -> {
            Vec3d pos = position.getValue(ctx);
            BlockState blockState = ctx.evaluator.world.getBlockState(BlockPos.ofFloored(pos));
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