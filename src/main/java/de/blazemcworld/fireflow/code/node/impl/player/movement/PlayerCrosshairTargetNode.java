package de.blazemcworld.fireflow.code.node.impl.player.movement;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.type.VectorType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

public class PlayerCrosshairTargetNode extends Node {

    public PlayerCrosshairTargetNode() {
        super("player_crosshair_target", "Player Crosshair Target", "Checks the block the player is currently looking at. Ignores fluids unless set to true.", Items.TIPPED_ARROW);

        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<Boolean> checkFluids = new Input<>("fluids", "Fluids", ConditionType.INSTANCE);
        Output<Vec3d> position = new Output<>("position", "Position", VectorType.INSTANCE);
        Output<String> block = new Output<>("block", "Block", StringType.INSTANCE);

        position.valueFrom((ctx) -> player.getValue(ctx).tryGet(ctx, p -> {
                    HitResult result = p.raycast(p.getAttributeValue(EntityAttributes.BLOCK_INTERACTION_RANGE), 0f, checkFluids.getValue(ctx));
                    if (result.getType() == HitResult.Type.MISS) return p.getEyePos();
                    return result.getPos();
                }, Vec3d.ZERO
        ));

        block.valueFrom((ctx) -> player.getValue(ctx).tryGet(ctx, p -> {
                    HitResult result = p.raycast(p.getAttributeValue(EntityAttributes.BLOCK_INTERACTION_RANGE), 0f, checkFluids.getValue(ctx));
                    if (!(result instanceof BlockHitResult bhit)) return "air";
                    return Registries.BLOCK.getId(ctx.evaluator.world.getBlockState(bhit.getBlockPos()).getBlock()).getPath();
                }, "air"
        ));
    }

    @Override
    public Node copy() {
        return new PlayerCrosshairTargetNode();
    }

}
