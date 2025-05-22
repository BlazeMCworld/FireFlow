package de.blazemcworld.fireflow.mixin;

import de.blazemcworld.fireflow.code.CodeInteraction;
import de.blazemcworld.fireflow.code.EditOrigin;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceManager;
import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void handleInteract(Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (!((PlayerEntity) (Object) this instanceof ServerPlayerEntity player)) return;

        Space space = SpaceManager.getSpaceForPlayer(player);
        if (ModeManager.getFor(player) == ModeManager.Mode.CODE && space != null && hand == Hand.MAIN_HAND) {
            space.editor.handleInteraction(EditOrigin.ofPlayer(player), CodeInteraction.Type.RIGHT_CLICK);
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void handleAttack(Entity target, CallbackInfo ci) {
        if (!((PlayerEntity) (Object) this instanceof ServerPlayerEntity player)) return;

        Space space = SpaceManager.getSpaceForPlayer(player);
        if (ModeManager.getFor(player) == ModeManager.Mode.CODE && space != null) {
            space.editor.handleInteraction(EditOrigin.ofPlayer(player), CodeInteraction.Type.LEFT_CLICK);
            ci.cancel();
        }
    }
}
