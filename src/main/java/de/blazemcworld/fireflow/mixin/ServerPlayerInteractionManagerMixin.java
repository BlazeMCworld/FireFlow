package de.blazemcworld.fireflow.mixin;

import de.blazemcworld.fireflow.space.Lobby;
import de.blazemcworld.fireflow.space.PlayWorld;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceManager;
import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

    @Shadow protected ServerWorld world;

    @Shadow @Final protected ServerPlayerEntity player;

    @Inject(method = "tryBreakBlock", at = @At("HEAD"), cancellable = true)
    private void preventBreaking(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Space space = SpaceManager.getSpaceForWorld(world);
        if (space != null && space.playWorld == world) {
            if (ModeManager.getFor(player) == ModeManager.Mode.PLAY && space.evaluator.onBreakBlock(player, pos)) {
                cir.setReturnValue(false);
            }
            if (ModeManager.getFor(player) == ModeManager.Mode.BUILD) return;
            return;
        }
        if (player.hasPermissionLevel(4) && player.getGameMode() == GameMode.CREATIVE && world == Lobby.world) return;
        cir.setReturnValue(false);
    }

    @Inject(method = "interactItem", at = @At("HEAD"), cancellable = true)
    private void useItem(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (world == Lobby.world) {
            Lobby.onUseItem(player, stack);
            cir.setReturnValue(ActionResult.FAIL);
            return;
        }
        if (world instanceof PlayWorld play && ModeManager.getFor(player) == ModeManager.Mode.PLAY) {
            if (play.space.evaluator.onUseItem(player, stack, hand)) {
                cir.setReturnValue(ActionResult.FAIL);
                return;
            }
        }
    }

}
