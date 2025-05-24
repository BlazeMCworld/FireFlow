package de.blazemcworld.fireflow.code.node.impl.world;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ListType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.value.ListValue;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class BlockIdListNode extends Node {

    private static final ListValue<String> constant = new ListValue<>(StringType.INSTANCE, collect());

    private static List<String> collect() {
        List<String> list = new ArrayList<>();
        for (Identifier id : Registries.BLOCK.getIds()) {
            list.add(id.getPath());
        }
        return list;
    }

    public BlockIdListNode() {
        super("block_id_list", "Block ID List", "Returns a list of all block ids in minecraft.", Items.KNOWLEDGE_BOOK);

        Output<ListValue<String>> list = new Output<>("list", "List", ListType.of(StringType.INSTANCE));
        list.valueFrom(ctx -> constant);
    }

    @Override
    public Node copy() {
        return new BlockIdListNode();
    }

}
