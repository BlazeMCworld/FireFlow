package de.blazemcworld.fireflow.code.node.impl.player.movement;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.type.VectorType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class PlayerStandingBlockNode extends Node {

    public PlayerStandingBlockNode() {
        super("player_standing_block", "Player Standing Block", "Checks if the player is standing on something or floating, and which block it is.", Items.BAMBOO_PRESSURE_PLATE);

        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Output<Vec3> position = new Output<>("position", "Position", VectorType.INSTANCE);
        Output<String> block = new Output<>("block", "Block", StringType.INSTANCE);
        Output<Boolean> floating = new Output<>("floating", "Floating", ConditionType.INSTANCE);

        position.valueFrom((ctx) -> player.getValue(ctx).tryGet(ctx, p ->
                new Vec3(p.mainSupportingBlockPos.orElse(BlockPos.ZERO)), Vec3.ZERO
        ));

        block.valueFrom((ctx) -> player.getValue(ctx).tryGet(ctx, p -> p.mainSupportingBlockPos
                .map(pos -> BuiltInRegistries.BLOCK.getKey(ctx.evaluator.level.getBlockState(pos).getBlock()).getPath()).orElse("unknown"), "unknown"
        ));

        floating.valueFrom((ctx) -> player.getValue(ctx).tryGet(ctx, p -> p.mainSupportingBlockPos.isEmpty(), false));
    }

    @Override
    public Node copy() {
        return new PlayerStandingBlockNode();
    }

}
