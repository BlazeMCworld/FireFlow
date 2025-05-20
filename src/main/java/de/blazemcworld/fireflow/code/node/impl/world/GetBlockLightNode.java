package de.blazemcworld.fireflow.code.node.impl.world;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.VectorType;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;

public class GetBlockLightNode extends Node {

    public GetBlockLightNode() {
        super("get_block_light", "Get Block Light", "Gets the light level of a block", Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE);

        Input<Vec3d> position = new Input<>("position", "Position", VectorType.INSTANCE);
        Output<Double> light = new Output<>("light", "Light", NumberType.INSTANCE);
        Output<Double> blockLight = new Output<>("block_light", "Block Light", NumberType.INSTANCE);
        Output<Double> skyLight = new Output<>("sky_light", "Sky Light", NumberType.INSTANCE);

        light.valueFrom((ctx) -> (double) ctx.evaluator.world.getLightingProvider().getLight(BlockPos.ofFloored(position.getValue(ctx)), 0));
        blockLight.valueFrom((ctx) -> (double) ctx.evaluator.world.getLightingProvider().get(LightType.BLOCK).getLightLevel(BlockPos.ofFloored(position.getValue(ctx))));
        skyLight.valueFrom((ctx) -> (double) ctx.evaluator.world.getLightingProvider().get(LightType.SKY).getLightLevel(BlockPos.ofFloored(position.getValue(ctx))));
    }

    @Override
    public Node copy() {
        return new GetBlockLightNode();
    }
}
