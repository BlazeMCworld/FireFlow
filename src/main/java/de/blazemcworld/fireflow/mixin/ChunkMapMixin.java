package de.blazemcworld.fireflow.mixin;

import de.blazemcworld.fireflow.code.CodeLevel;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkMap.class)
public class ChunkMapMixin {

    @Shadow
    @Final
    private ServerLevel level;

    @Inject(method = "save", at = @At("HEAD"), cancellable = true)
    private void fireflow$save(ChunkAccess chunk, CallbackInfoReturnable<Boolean> cir) {
        if (level instanceof CodeLevel) cir.setReturnValue(false);
    }

}
