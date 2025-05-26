package de.blazemcworld.fireflow.code.node.impl.event.world;

import de.blazemcworld.fireflow.code.CodeEvaluator;
import de.blazemcworld.fireflow.code.CodeThread;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.*;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class OnPlayerInteractBlockNode extends Node {

    private final Output<Void> signal;
    private final Output<PlayerValue> player;
    private final Output<Vec3d> position;
    private final Output<Vec3d> side;
    private final Output<Boolean> isMainHand;

    public OnPlayerInteractBlockNode() {
        super("on_player_interact_block", "On Player Interact Block", "Emits a signal when a player attempts to interact with a block.", Items.OAK_BUTTON);

        signal = new Output<>("signal", "Signal", SignalType.INSTANCE);
        player = new Output<>("player", "Player", PlayerType.INSTANCE);
        position = new Output<>("position", "Position", VectorType.INSTANCE);
        side = new Output<>("side", "Side", VectorType.INSTANCE);
        isMainHand = new Output<>("is_main_hand", "Is Main Hand", ConditionType.INSTANCE);

        player.valueFromScope();
        position.valueFromScope();
        side.valueFromScope();
        isMainHand.valueFromScope();
    }

    @Override
    public Node copy() {
        return new OnPlayerInteractBlockNode();
    }

    public boolean onInteractBlock(CodeEvaluator codeEvaluator, ServerPlayerEntity player, BlockPos pos, Direction side, Hand hand, boolean cancel) {
        CodeThread thread = codeEvaluator.newCodeThread();
        thread.context.cancelled = cancel;
        thread.setScopeValue(this.player, new PlayerValue(player));
        thread.setScopeValue(this.position, Vec3d.of(pos));
        thread.setScopeValue(this.side, side.getDoubleVector());
        thread.setScopeValue(this.isMainHand, hand == Hand.MAIN_HAND);
        thread.sendSignal(signal);
        thread.clearQueue();
        return thread.context.cancelled;
    }
}
