package de.blazemcworld.fireflow.mixin;

import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerExplosion.class)
public class ServerExplosionMixin {

    @Shadow
    @Final
    private ServerLevel level;

    @Inject(method = "interactsWithBlocks", at = @At("HEAD"), cancellable = true)
    public void fireflow$preventDestruction(CallbackInfoReturnable<Boolean> cir) {
        Space space = SpaceManager.getSpaceForLevel(level);
        if (space != null && space.playLevel == level) return;
        cir.setReturnValue(false);
    }

}
