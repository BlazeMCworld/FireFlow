package de.blazemcworld.fireflow.mixin;

import com.mojang.brigadier.ParseResults;
import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.code.CodeInteraction;
import de.blazemcworld.fireflow.code.EditOrigin;
import de.blazemcworld.fireflow.space.Lobby;
import de.blazemcworld.fireflow.space.PlayLevel;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceManager;
import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void fireflow$ignore(CallbackInfo ci) {
        if (player.level() instanceof PlayLevel s) {
            if (s.thread != Thread.currentThread()) ci.cancel();
        }
    }

    @Redirect(method = "performUnsignedChatCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/Commands;performCommand(Lcom/mojang/brigadier/ParseResults;Ljava/lang/String;)V"))
    private void fireflow$queue(Commands instance, ParseResults<CommandSourceStack> parseResults, String command) {
        if (player.level() instanceof PlayLevel playLevel) {
            playLevel.submit(() -> instance.performCommand(parseResults, command));
            return;
        }
        instance.performCommand(parseResults, command);
    }

    @Inject(method = "handlePlayerAction", at = @At("HEAD"), cancellable = true)
    private void fireflow$preventActions(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
        PacketUtils.ensureRunningOnSameThread(packet, (ServerGamePacketListenerImpl) (Object) this, player.level());
        if (packet.getAction() == ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND
                || packet.getAction() == ServerboundPlayerActionPacket.Action.DROP_ITEM
                || packet.getAction() == ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS) {
            Space space = SpaceManager.getSpaceForPlayer(player);
            if (space != null && ModeManager.getFor(player) == ModeManager.Mode.PLAY) {
                if (packet.getAction() == ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND) {
                    if (!space.evaluator.onSwapHands(player)) return;
                }
                if (packet.getAction() == ServerboundPlayerActionPacket.Action.DROP_ITEM
                        || packet.getAction() == ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS) {
                    if (!space.evaluator.onDropItem(player)) return;
                }
            }
            if (space != null && ModeManager.getFor(player) == ModeManager.Mode.CODE && packet.getAction() == ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND) {
                space.editor.handleInteraction(EditOrigin.ofPlayer(player), CodeInteraction.Type.SWAP_HANDS);
            }
            ci.cancel();
            player.inventoryMenu.sendAllDataToRemote();
        }
    }

    @Inject(method = "handleContainerClick", at = @At("HEAD"), cancellable = true)
    private void fireflow$preventClick(ServerboundContainerClickPacket packet, CallbackInfo ci) {
        PacketUtils.ensureRunningOnSameThread(packet, (ServerGamePacketListenerImpl) (Object) this, player.level());
        if (player.inventoryMenu instanceof InventoryMenu) return;
        if (player.level() != Lobby.level) return;
        if (FireFlow.server.getProfilePermissions(player.nameAndId()).level().isEqualOrHigherThan(PermissionLevel.ADMINS) && player.gameMode() == GameType.CREATIVE) return;
        ci.cancel();
        player.inventoryMenu.sendAllDataToRemote();
    }

    @Redirect(method = "lambda$handleChat$1", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;)V"))
    private void fireflow$handleChat(ServerGamePacketListenerImpl instance, PlayerChatMessage message) {
        Space space = SpaceManager.getSpaceForPlayer(player);
        if (space != null && ModeManager.getFor(player) == ModeManager.Mode.CODE) {
            if (space.editor.handleInteraction(EditOrigin.ofPlayer(player), CodeInteraction.Type.CHAT, message.signedContent())) {
                return;
            }
        }
        if (space != null && ModeManager.getFor(player) == ModeManager.Mode.PLAY) {
            if (space.evaluator.onChat(player, message.signedContent())) {
                return;
            }
        }
        instance.broadcastChatMessage(message);
    }

    @Inject(method = "handleAnimate", at = @At("HEAD"), cancellable = true)
    private void fireflow$handleSwing(ServerboundSwingPacket packet, CallbackInfo ci) {
        PacketUtils.ensureRunningOnSameThread(packet, (ServerGamePacketListenerImpl) (Object) this, player.level());
        if (packet.getHand() != InteractionHand.MAIN_HAND) return;

        Space space = SpaceManager.getSpaceForPlayer(player);
        if (space == null || ModeManager.getFor(player) != ModeManager.Mode.PLAY) return;
        if (space.evaluator.onSwingHand(player, packet.getHand() == InteractionHand.MAIN_HAND)) ci.cancel();
    }

    @Inject(method = "handlePlayerAbilities", at = @At("HEAD"), cancellable = true)
    private void fireflow$flightChange(ServerboundPlayerAbilitiesPacket packet, CallbackInfo ci) {
        PacketUtils.ensureRunningOnSameThread(packet, (ServerGamePacketListenerImpl) (Object) this, player.level());
        boolean isFlying = player.getAbilities().flying;
        if (isFlying == packet.isFlying()) return;

        Space space = SpaceManager.getSpaceForPlayer(player);
        if (space == null || ModeManager.getFor(player) != ModeManager.Mode.PLAY) return;
        if (space.evaluator.shouldCancelFlight(player, packet.isFlying())) {
            ci.cancel();
            player.onUpdateAbilities();
        }
    }

    @Inject(method = "handlePlayerInput", at = @At("HEAD"))
    private void fireflow$handleInput(ServerboundPlayerInputPacket packet, CallbackInfo ci) {
        PacketUtils.ensureRunningOnSameThread(packet, (ServerGamePacketListenerImpl) (Object) this, player.level());

        Space space = SpaceManager.getSpaceForPlayer(player);
        if (space == null || ModeManager.getFor(player) != ModeManager.Mode.PLAY) return;

        if (packet.input().shift()) {
            if (!player.isShiftKeyDown()) space.evaluator.onStartSneaking(player);
        }

        if (!packet.input().shift()) {
            if (player.isShiftKeyDown()) space.evaluator.onStopSneaking(player);
        }
    }

    @Inject(method = "handlePlayerCommand", at = @At("HEAD"))
    private void fireflow$onClientCommand(ServerboundPlayerCommandPacket packet, CallbackInfo ci) {
        PacketUtils.ensureRunningOnSameThread(packet, (ServerGamePacketListenerImpl) (Object) this, player.level());

        Space space = SpaceManager.getSpaceForPlayer(player);
        if (space == null || ModeManager.getFor(player) != ModeManager.Mode.PLAY) return;

        if (packet.getAction() == ServerboundPlayerCommandPacket.Action.START_SPRINTING) {
            if (!player.isSprinting()) space.evaluator.onStartSprinting(player);
        }
        if (packet.getAction() == ServerboundPlayerCommandPacket.Action.STOP_SPRINTING) {
            if (player.isSprinting()) space.evaluator.onStopSprinting(player);
        }
    }

}
