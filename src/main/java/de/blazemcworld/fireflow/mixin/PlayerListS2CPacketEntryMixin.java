package de.blazemcworld.fireflow.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import de.blazemcworld.fireflow.util.DummyPlayer;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(PlayerListS2CPacket.Entry.class)
public class PlayerListS2CPacketEntryMixin {

    @ModifyConstant(method = "<init>(Lnet/minecraft/server/network/ServerPlayerEntity;)V", constant = @Constant(intValue = 1))
    private static int hideDummy(int constant, @Local(argsOnly = true) ServerPlayerEntity player) {
        return player instanceof DummyPlayer ? 0 : constant;
    }

}
