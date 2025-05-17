package de.blazemcworld.fireflow.code.node.impl.player.info;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.type.VectorType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PlayerStandingBlockNode extends Node {

    public PlayerStandingBlockNode() {
        super("player_standing_block", "Player Standing Block", "Checks if the player is standing on something or floating, and which block it is.", Items.RECOVERY_COMPASS);

        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Output<Vec3d> position = new Output<>("position", "Position", VectorType.INSTANCE);
        Output<String> block = new Output<>("block", "Block", StringType.INSTANCE);
        Output<Boolean> floating = new Output<>("floating", "Floating", ConditionType.INSTANCE);

        position.valueFrom((ctx) -> player.getValue(ctx).tryGet(ctx, p ->
                new Vec3d(p.supportingBlockPos.orElse(BlockPos.ORIGIN)), Vec3d.ZERO
        ));

        block.valueFrom((ctx) -> player.getValue(ctx).tryGet(ctx, p -> p.supportingBlockPos
                .map(pos -> Registries.BLOCK.getId(ctx.evaluator.world.getBlockState(pos).getBlock()).getPath()).orElse("unknown"), "unknown"
        ));

        floating.valueFrom((ctx) -> player.getValue(ctx).tryGet(ctx, p -> p.supportingBlockPos.isEmpty(), false));
    }

    @Override
    public Node copy() {
        return new PlayerStandingBlockNode();
    }

}
