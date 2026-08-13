package de.blazemcworld.fireflow.code.node.impl.player.movement;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.type.VectorType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PlayerCrosshairTargetNode extends Node {

    public PlayerCrosshairTargetNode() {
        super("player_crosshair_target", "Player Crosshair Target", "Checks the block the player is currently looking at. Ignores fluids unless set to true.", Items.TIPPED_ARROW);

        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<Boolean> checkFluids = new Input<>("fluids", "Fluids", ConditionType.INSTANCE);
        Output<Vec3> position = new Output<>("position", "Position", VectorType.INSTANCE);
        Output<String> block = new Output<>("block", "Block", StringType.INSTANCE);

        position.valueFrom((ctx) -> player.getValue(ctx).tryGet(ctx, p -> {
                    HitResult result = p.pick(p.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE), 0f, checkFluids.getValue(ctx));
                    if (result.getType() == BlockHitResult.Type.MISS) return p.getEyePosition();
                    return result.getLocation();
                }, Vec3.ZERO
        ));

        block.valueFrom((ctx) -> player.getValue(ctx).tryGet(ctx, p -> {
                    HitResult result = p.pick(p.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE), 0f, checkFluids.getValue(ctx));
                    if (!(result instanceof BlockHitResult bhit)) return "air";
                    return BuiltInRegistries.BLOCK.getKey(ctx.evaluator.level.getBlockState(bhit.getBlockPos()).getBlock()).getPath();
                }, "air"
        ));
    }

    @Override
    public Node copy() {
        return new PlayerCrosshairTargetNode();
    }

}
