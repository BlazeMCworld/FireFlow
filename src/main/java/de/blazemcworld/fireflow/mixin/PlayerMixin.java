package de.blazemcworld.fireflow.mixin;

import de.blazemcworld.fireflow.code.CodeInteraction;
import de.blazemcworld.fireflow.code.EditOrigin;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceManager;
import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {

    @Inject(method = "interactOn", at = @At("HEAD"), cancellable = true)
    private void fireflow$handleInteract(Entity entity, InteractionHand hand, Vec3 location, CallbackInfoReturnable<InteractionResult> cir) {
        if (!((Player) (Object) this instanceof ServerPlayer player)) return;

        Space space = SpaceManager.getSpaceForPlayer(player);
        if (ModeManager.getFor(player) == ModeManager.Mode.CODE && space != null && hand == InteractionHand.MAIN_HAND) {
            space.editor.handleInteraction(EditOrigin.ofPlayer(player), CodeInteraction.Type.RIGHT_CLICK);
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void fireflow$handleAttack(Entity entity, CallbackInfo ci) {
        if (!((Player) (Object) this instanceof ServerPlayer player)) return;

        Space space = SpaceManager.getSpaceForPlayer(player);
        if (ModeManager.getFor(player) == ModeManager.Mode.CODE && space != null) {
            space.editor.handleInteraction(EditOrigin.ofPlayer(player), CodeInteraction.Type.LEFT_CLICK);
            ci.cancel();
        }
    }
}
