package de.blazemcworld.fireflow.code.node.impl.player.inventory;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.*;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;


public class SetPlayerArmorNode extends Node {

    public SetPlayerArmorNode() {
        super("set_player_armor", "Set Player Armor", "Changes the armor of a player", Material.DIAMOND_LEGGINGS);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<ItemStack> head = new Input<>("head", "Head", ItemType.INSTANCE);
        Input<ItemStack> chest = new Input<>("chest", "Chest", ItemType.INSTANCE);
        Input<ItemStack> legs = new Input<>("legs", "Legs", ItemType.INSTANCE);
        Input<ItemStack> feet = new Input<>("feet", "Feet", ItemType.INSTANCE);
        Input<String> behaviour = new Input<>("behaviour", "Behaviour", StringType.INSTANCE)
                .options("Clear", "Merge");
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            player.getValue(ctx).tryUse(ctx, p -> {
                boolean clearInv = behaviour.getValue(ctx).equals("Clear");
                ItemStack headItem = head.getValue(ctx);
                ItemStack chestItem = chest.getValue(ctx);
                ItemStack legsItem = legs.getValue(ctx);
                ItemStack feetItem = feet.getValue(ctx);
                if (!headItem.isEmpty() || clearInv) p.getInventory().setItem(39, headItem);
                if (!chestItem.isEmpty() || clearInv) p.getInventory().setItem(38, chestItem);
                if (!legsItem.isEmpty() || clearInv) p.getInventory().setItem(37, legsItem);
                if (!feetItem.isEmpty() || clearInv) p.getInventory().setItem(36, feetItem);
            });
            ctx.sendSignal(next);
        });

    }

    @Override
    public Node copy() {
        return new SetPlayerArmorNode();
    }

}


