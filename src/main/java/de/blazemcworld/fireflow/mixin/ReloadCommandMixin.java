package de.blazemcworld.fireflow.mixin;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.ReloadCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ReloadCommand.class)
public class ReloadCommandMixin {

    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    private static void fireflow$dontRegister(CommandDispatcher<CommandSourceStack> dispatcher, CallbackInfo ci) {
        ci.cancel();
    }

}
