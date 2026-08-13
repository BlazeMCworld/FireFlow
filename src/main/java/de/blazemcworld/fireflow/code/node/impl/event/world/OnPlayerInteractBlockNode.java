package de.blazemcworld.fireflow.code.node.impl.event.world;

import de.blazemcworld.fireflow.code.CodeEvaluator;
import de.blazemcworld.fireflow.code.CodeThread;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.VectorType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class OnPlayerInteractBlockNode extends Node {

    private final Output<Void> signal;
    private final Output<PlayerValue> player;
    private final Output<Vec3> position;
    private final Output<Vec3> side;
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

    public boolean onInteractBlock(CodeEvaluator codeEvaluator, ServerPlayer player, BlockPos pos, Direction side, InteractionHand hand, boolean cancel) {
        CodeThread thread = codeEvaluator.newCodeThread();
        thread.context.cancelled = cancel;
        thread.setScopeValue(this.player, new PlayerValue(player));
        thread.setScopeValue(this.position, Vec3.atCenterOf(pos));
        thread.setScopeValue(this.side, side.getUnitVec3());
        thread.setScopeValue(this.isMainHand, hand == InteractionHand.MAIN_HAND);
        thread.sendSignal(signal);
        thread.clearQueue();
        return thread.context.cancelled;
    }
}
