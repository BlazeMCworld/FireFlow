package de.blazemcworld.fireflow.code.node.impl.event.world;

import de.blazemcworld.fireflow.code.CodeEvaluator;
import de.blazemcworld.fireflow.code.CodeThread;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.*;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.Vec3;

public class OnPlayerPlaceBlockNode extends Node {

    private final Output<Void> signal;
    private final Output<PlayerValue> player;
    private final Output<Vec3> position;
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

    public boolean onPlaceBlock(CodeEvaluator codeEvaluator, BlockPlaceContext context, boolean cancel) {
        if (context.getPlayer() instanceof ServerPlayer p) {
            CodeThread thread = codeEvaluator.newCodeThread();
            thread.context.cancelled = cancel;
            thread.setScopeValue(this.player, new PlayerValue(p));
            thread.setScopeValue(this.position, Vec3.atCenterOf(context.getClickedPos()));
            thread.setScopeValue(this.item, context.getItemInHand());
            thread.setScopeValue(this.isMainHand, context.getHand() == InteractionHand.MAIN_HAND);
            thread.sendSignal(signal);
            thread.clearQueue();
            return thread.context.cancelled;
        }
        return false;
    }
}


