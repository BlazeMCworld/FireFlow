package de.blazemcworld.fireflow.code.node.impl.world;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.VectorType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

public class GetBlockLightNode extends Node {

    public GetBlockLightNode() {
        super("get_block_light", "Get Block Light", "Gets the light level of a block", Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE);

        Input<Vec3> position = new Input<>("position", "Position", VectorType.INSTANCE);
        Output<Double> light = new Output<>("light", "Light", NumberType.INSTANCE);
        Output<Double> blockLight = new Output<>("block_light", "Block Light", NumberType.INSTANCE);
        Output<Double> skyLight = new Output<>("sky_light", "Sky Light", NumberType.INSTANCE);

        light.valueFrom((ctx) -> (double) ctx.evaluator.level.getLightEngine().getRawBrightness(BlockPos.containing(position.getValue(ctx)), 0));
        blockLight.valueFrom((ctx) -> (double) ctx.evaluator.level.getLightEngine().getLayerListener(LightLayer.BLOCK).getLightValue(BlockPos.containing(position.getValue(ctx))));
        skyLight.valueFrom((ctx) -> (double) ctx.evaluator.level.getLightEngine().getLayerListener(LightLayer.SKY).getLightValue(BlockPos.containing(position.getValue(ctx))));
    }

    @Override
    public Node copy() {
        return new GetBlockLightNode();
    }
}
