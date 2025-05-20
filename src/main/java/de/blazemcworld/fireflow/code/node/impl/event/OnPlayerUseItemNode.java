package de.blazemcworld.fireflow.code.node.impl.event;

import de.blazemcworld.fireflow.code.CodeEvaluator;
import de.blazemcworld.fireflow.code.CodeThread;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.ItemType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

public class OnPlayerUseItemNode extends Node {

    private final Output<Void> signal;
    private final Output<PlayerValue> player;
    private final Output<ItemStack> item;
    private final Output<Boolean> isMainHand;

    public OnPlayerUseItemNode() {
        super("on_player_use_item", "On Player Use Item", "Emits a signal when a player attempts to use an item.", Items.IRON_HOE);

        signal = new Output<>("signal", "Signal", SignalType.INSTANCE);
        player = new Output<>("player", "Player", PlayerType.INSTANCE);
        item = new Output<>("item", "Item", ItemType.INSTANCE);
        isMainHand = new Output<>("is_main_hand", "Is Main Hand", ConditionType.INSTANCE);

        player.valueFromScope();
        item.valueFromScope();
        isMainHand.valueFromScope();
    }

    @Override
    public Node copy() {
        return new OnPlayerUseItemNode();
    }

    public boolean onUseItem(CodeEvaluator codeEvaluator, ServerPlayerEntity player, ItemStack stack, Hand hand, boolean cancel) {
        CodeThread thread = codeEvaluator.newCodeThread();
        thread.context.cancelled = cancel;
        thread.setScopeValue(this.player, new PlayerValue(player));
        thread.setScopeValue(this.item, stack);
        thread.setScopeValue(this.isMainHand, hand == Hand.MAIN_HAND);
        thread.sendSignal(signal);
        thread.clearQueue();
        return thread.context.cancelled;
    }
}
