package de.blazemcworld.fireflow.code.node.impl.event;

import de.blazemcworld.fireflow.code.CodeEvaluator;
import de.blazemcworld.fireflow.code.CodeThread;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.*;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class OnPlayerPlaceBlockNode extends Node {

    private final Output<Void> signal;
    private final Output<PlayerValue> player;
    private final Output<Vec3d> position;
    private final Output<ItemStack> item;
    private final Output<Boolean> isMainHand;

    public OnPlayerPlaceBlockNode() {
        super("on_player_place_block", "On Player Place Block", "Emits a signal when a player places a block.", Items.GOLDEN_SHOVEL);

        signal = new Output<>("signal", "Signal", SignalType.INSTANCE);
        player = new Output<>("player", "Player", PlayerType.INSTANCE);
        position = new Output<>("position", "Position", VectorType.INSTANCE);
        item = new Output<>("item", "Item", ItemType.INSTANCE);
        isMainHand = new Output<>("is_main_hand", "Is Main Hand", ConditionType.INSTANCE);
        player.valueFromScope();
        position.valueFromScope();
        item.valueFromScope();
        isMainHand.valueFromScope();
    }

    @Override
    public Node copy() {
        return new OnPlayerPlaceBlockNode();
    }

    public boolean onPlaceBlock(CodeEvaluator codeEvaluator, ItemPlacementContext context, boolean cancel) {
        if (context.getPlayer() instanceof ServerPlayerEntity p) {
            CodeThread thread = codeEvaluator.newCodeThread();
            thread.eventCancelled = cancel;
            thread.setScopeValue(this.player, new PlayerValue(p));
            thread.setScopeValue(this.position, Vec3d.of(context.getBlockPos()));
            thread.setScopeValue(this.item, context.getStack());
            thread.setScopeValue(this.isMainHand, context.getHand() == Hand.MAIN_HAND);
            thread.sendSignal(signal);
            thread.clearQueue();
            return thread.eventCancelled;
        }
        return false;
    }
}


