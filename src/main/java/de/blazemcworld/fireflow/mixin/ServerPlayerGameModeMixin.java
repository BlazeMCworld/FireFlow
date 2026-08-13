package de.blazemcworld.fireflow.mixin;

import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.space.Lobby;
import de.blazemcworld.fireflow.space.PlayLevel;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceManager;
import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {

    @Shadow protected ServerLevel level;

    @Shadow @Final protected ServerPlayer player;

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void fireflow$preventBreaking(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Space space = SpaceManager.getSpaceForLevel(level);
        if (space != null && space.playLevel == level) {
            if (ModeManager.getFor(player) == ModeManager.Mode.PLAY && space.evaluator.onBreakBlock(player, pos)) {
                cir.setReturnValue(false);
            }
            if (ModeManager.getFor(player) == ModeManager.Mode.BUILD) return;
            return;
        }
        if (FireFlow.server.getProfilePermissions(player.nameAndId()).level().isEqualOrHigherThan(PermissionLevel.ADMINS) && player.gameMode() == GameType.CREATIVE && level == Lobby.level) return;
        cir.setReturnValue(false);
    }

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void fireflow$interactBlock(ServerPlayer player, Level level, ItemStack itemStack, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (level instanceof PlayLevel play && ModeManager.getFor(player) == ModeManager.Mode.PLAY) {
            if (play.space.evaluator.onInteractBlock(player, hitResult.getBlockPos(), hitResult.getDirection(), hand)) {
                cir.setReturnValue(InteractionResult.FAIL);
                return;
            }
        }
    }

    @Inject(method = "useItem", at = @At("HEAD"), cancellable = true)
    private void fireflow$useItem(ServerPlayer player, Level level, ItemStack itemStack, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (level == Lobby.level) {
            Lobby.onUseItem(player, itemStack);
            cir.setReturnValue(InteractionResult.FAIL);
            return;
        }
        if (level instanceof PlayLevel play && ModeManager.getFor(player) == ModeManager.Mode.PLAY) {
            if (play.space.evaluator.onUseItem(player, itemStack, hand)) {
                cir.setReturnValue(InteractionResult.FAIL);
                return;
            }
        }
    }

}
