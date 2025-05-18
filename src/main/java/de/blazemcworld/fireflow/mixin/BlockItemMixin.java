package de.blazemcworld.fireflow.mixin;

import de.blazemcworld.fireflow.space.Lobby;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceManager;
import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At("HEAD"), cancellable = true)
    private void preventPlacement(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (!(context.getWorld() instanceof ServerWorld world)) return;
        if (context.getPlayer() == null) return;
        Space space = SpaceManager.getSpaceForWorld(world);
        if (context.getPlayer() instanceof ServerPlayerEntity p && space != null && space.playWorld == world) {
            if (ModeManager.getFor(p) == ModeManager.Mode.PLAY && space.evaluator.onPlaceBlock(context)) {
                cir.setReturnValue(ActionResult.FAIL);
            }
            return;
        }
        if (context.getPlayer().hasPermissionLevel(4) && context.getPlayer().getGameMode() == GameMode.CREATIVE && world == Lobby.world) return;
        cir.setReturnValue(ActionResult.FAIL);
    }
}
