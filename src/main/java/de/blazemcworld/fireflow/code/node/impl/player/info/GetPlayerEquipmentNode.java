package de.blazemcworld.fireflow.code.node.impl.player.info;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ItemType;
import de.blazemcworld.fireflow.code.type.ListType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.value.ListValue;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.List;

public class GetPlayerEquipmentNode extends Node {

    public GetPlayerEquipmentNode() {
        super("get_player_equipment", "Get Player Equipment", "Gets a list of the player's equipment.", Items.IRON_CHESTPLATE);

        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Output<ListValue<ItemStack>> main = new Output<>("main", "Main", ListType.of(ItemType.INSTANCE));

        main.valueFrom((ctx) -> player.getValue(ctx).tryGet(ctx, p ->
                new ListValue<>(ItemType.INSTANCE, List.of(
                        p.getEquippedStack(EquipmentSlot.HEAD),
                        p.getEquippedStack(EquipmentSlot.CHEST),
                        p.getEquippedStack(EquipmentSlot.LEGS),
                        p.getEquippedStack(EquipmentSlot.FEET)
                )), new ListValue<>(ItemType.INSTANCE)
        ));
    }

    @Override
    public Node copy() {
        return new GetPlayerEquipmentNode();
    }

}
