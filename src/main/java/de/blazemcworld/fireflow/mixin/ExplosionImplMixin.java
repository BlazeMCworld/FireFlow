package de.blazemcworld.fireflow.mixin;

import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.explosion.ExplosionImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExplosionImpl.class)
public class ExplosionImplMixin {

    @Shadow
    @Final
    private ServerWorld world;

    @Inject(method = "shouldDestroyBlocks", at = @At("HEAD"), cancellable = true)
    public void preventDestruction(CallbackInfoReturnable<Boolean> cir) {
        Space space = SpaceManager.getSpaceForWorld(world);
        if (space != null && space.playWorld == world) return;
        cir.setReturnValue(false);
    }

}
