package de.blazemcworld.fireflow.mixin;

import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.space.Lobby;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceManager;
import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    @Inject(method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;", at = @At("HEAD"), cancellable = true)
    private void fireflow$preventPlacement(BlockPlaceContext placeContext, CallbackInfoReturnable<InteractionResult> cir) {
        if (!(placeContext.getLevel() instanceof ServerLevel level)) return;
        if (placeContext.getPlayer() == null) return;
        Space space = SpaceManager.getSpaceForLevel(level);
        if (placeContext.getPlayer() instanceof ServerPlayer p && space != null && space.playLevel == level) {
            if (ModeManager.getFor(p) == ModeManager.Mode.PLAY && space.evaluator.onPlaceBlock(placeContext)) {
                cir.setReturnValue(InteractionResult.FAIL);
                p.inventoryMenu.sendAllDataToRemote();
            }
            return;
        }
        if (FireFlow.server.getProfilePermissions(placeContext.getPlayer().nameAndId()).level().isEqualOrHigherThan(PermissionLevel.ADMINS) && placeContext.getPlayer().gameMode() == GameType.CREATIVE && level == Lobby.level) return;
        cir.setReturnValue(InteractionResult.FAIL);
    }
}
