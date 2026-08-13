package de.blazemcworld.fireflow.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import de.blazemcworld.fireflow.util.DummyPlayer;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ClientboundPlayerInfoUpdatePacket.Entry.class)
public class PlayerListS2CPacketEntryMixin {

    @ModifyConstant(method = "<init>(Lnet/minecraft/server/level/ServerPlayer;)V", constant = @Constant(intValue = 1))
    private static int fireflow$hideDummy(int constant, @Local(argsOnly = true, name = "player") ServerPlayer player) {
        return player instanceof DummyPlayer ? 0 : constant;
    }

}
