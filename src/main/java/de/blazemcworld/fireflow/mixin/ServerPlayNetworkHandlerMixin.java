package de.blazemcworld.fireflow.mixin;

import com.mojang.brigadier.ParseResults;
import de.blazemcworld.fireflow.code.CodeInteraction;
import de.blazemcworld.fireflow.code.EditOrigin;
import de.blazemcworld.fireflow.inventory.InventoryMenu;
import de.blazemcworld.fireflow.space.Lobby;
import de.blazemcworld.fireflow.space.PlayWorld;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceManager;
import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void ignore(CallbackInfo ci) {
        if (player.getServerWorld() instanceof PlayWorld s) {
            if (s.thread != Thread.currentThread()) ci.cancel();
        }
    }

    @Redirect(method = "executeCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/CommandManager;execute(Lcom/mojang/brigadier/ParseResults;Ljava/lang/String;)V"))
    private void queue(CommandManager instance, ParseResults<ServerCommandSource> parseResults, String command) {
        if (player.getServerWorld() instanceof PlayWorld playWorld) {
            playWorld.submit(() -> instance.execute(parseResults, command));
            return;
        }
        instance.execute(parseResults, command);
    }

    @Inject(method = "onPlayerAction", at = @At("HEAD"), cancellable = true)
    private void preventActions(PlayerActionC2SPacket packet, CallbackInfo ci) {
        NetworkThreadUtils.forceMainThread(packet, (ServerPlayNetworkHandler) (Object) this, player.getServerWorld());
        if (packet.getAction() == PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND
                || packet.getAction() == PlayerActionC2SPacket.Action.DROP_ITEM
                || packet.getAction() == PlayerActionC2SPacket.Action.DROP_ALL_ITEMS) {
            Space space = SpaceManager.getSpaceForPlayer(player);
            if (space != null && ModeManager.getFor(player) == ModeManager.Mode.PLAY) {
                if (packet.getAction() == PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND) {
                    if (!space.evaluator.onSwapHands(player)) return;
                }
                if (packet.getAction() == PlayerActionC2SPacket.Action.DROP_ITEM
                        || packet.getAction() == PlayerActionC2SPacket.Action.DROP_ALL_ITEMS) {
                    if (!space.evaluator.onDropItem(player)) return;
                }
            }
            if (space != null && ModeManager.getFor(player) == ModeManager.Mode.CODE && packet.getAction() == PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND) {
                space.editor.handleInteraction(EditOrigin.ofPlayer(player), CodeInteraction.Type.SWAP_HANDS);
            }
            ci.cancel();
            player.playerScreenHandler.syncState();
        }
    }

    @Inject(method = "onClickSlot", at = @At("HEAD"), cancellable = true)
    private void preventClick(ClickSlotC2SPacket packet, CallbackInfo ci) {
        NetworkThreadUtils.forceMainThread(packet, (ServerPlayNetworkHandler) (Object) this, player.getServerWorld());
        if (player.currentScreenHandler instanceof InventoryMenu) return;
        if (player.getServerWorld() != Lobby.world) return;
        if (player.hasPermissionLevel(4) && player.getGameMode() == GameMode.CREATIVE) return;
        if (ModeManager.getFor(player) == ModeManager.Mode.CODE) return;
        ci.cancel();
        player.playerScreenHandler.syncState();
    }

    @Redirect(method = "method_45064", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;handleDecoratedMessage(Lnet/minecraft/network/message/SignedMessage;)V"))
    private void handleChat(ServerPlayNetworkHandler instance, SignedMessage message) {
        Space space = SpaceManager.getSpaceForPlayer(player);
        if (space != null && ModeManager.getFor(player) == ModeManager.Mode.CODE) {
            if (space.editor.handleInteraction(EditOrigin.ofPlayer(player), CodeInteraction.Type.CHAT, message.getSignedContent())) {
                return;
            }
        }
        if (space != null && ModeManager.getFor(player) == ModeManager.Mode.PLAY) {
            if (space.evaluator.onChat(player, message.getSignedContent())) {
                return;
            }
        }
        instance.handleDecoratedMessage(message);
    }

    @Inject(method = "onHandSwing", at = @At("HEAD"), cancellable = true)
    private void handleSwing(HandSwingC2SPacket packet, CallbackInfo ci) {
        NetworkThreadUtils.forceMainThread(packet, (ServerPlayNetworkHandler) (Object) this, player.getServerWorld());
        if (packet.getHand() != Hand.MAIN_HAND) return;

        Space space = SpaceManager.getSpaceForPlayer(player);
        if (space == null || ModeManager.getFor(player) != ModeManager.Mode.PLAY) return;
        if (space.evaluator.onSwingHand(player, packet.getHand() == Hand.MAIN_HAND)) ci.cancel();
    }

    @Inject(method = "onUpdatePlayerAbilities", at = @At("HEAD"), cancellable = true)
    private void flightChange(UpdatePlayerAbilitiesC2SPacket packet, CallbackInfo ci) {
        NetworkThreadUtils.forceMainThread(packet, (ServerPlayNetworkHandler) (Object) this, player.getServerWorld());
        boolean isFlying = player.getAbilities().flying;
        if (isFlying == packet.isFlying()) return;

        Space space = SpaceManager.getSpaceForPlayer(player);
        if (space == null || ModeManager.getFor(player) != ModeManager.Mode.PLAY) return;
        if (space.evaluator.shouldCancelFlight(player, packet.isFlying())) {
            ci.cancel();
            player.sendAbilitiesUpdate();
        }
    }

    @Inject(method = "onClientCommand", at = @At("HEAD"))
    private void onClientCommand(ClientCommandC2SPacket packet, CallbackInfo ci) {
        NetworkThreadUtils.forceMainThread(packet, (ServerPlayNetworkHandler) (Object) this, player.getServerWorld());

        Space space = SpaceManager.getSpaceForPlayer(player);
        if (space == null || ModeManager.getFor(player) != ModeManager.Mode.PLAY) return;

        if (packet.getMode() == ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY) {
            if (player.isSneaking()) return;
            space.evaluator.onStartSneaking(player);
        }
        if (packet.getMode() == ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY) {
            if (!player.isSneaking()) return;
            space.evaluator.onStopSneaking(player);
        }
        if (packet.getMode() == ClientCommandC2SPacket.Mode.START_SPRINTING) {
            if (player.isSprinting()) return;
            space.evaluator.onStartSprinting(player);
        }
        if (packet.getMode() == ClientCommandC2SPacket.Mode.STOP_SPRINTING) {
            if (!player.isSprinting()) return;
            space.evaluator.onStopSprinting(player);
        }
    }

}
