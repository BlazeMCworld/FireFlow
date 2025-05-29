package de.blazemcworld.fireflow.mixin;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.*;
import net.minecraft.server.dedicated.command.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandManager.class)
public class CommandManagerMixin {

    @Shadow private CommandDispatcher<ServerCommandSource> dispatcher;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/CommandDispatcher;setConsumer(Lcom/mojang/brigadier/ResultConsumer;)V", remap = false))
    private void init(CommandManager.RegistrationEnvironment environment, CommandRegistryAccess commandRegistryAccess, CallbackInfo ci) {
        dispatcher = new CommandDispatcher<>();
        CommandRegistrationCallback.EVENT.invoker().register(dispatcher, commandRegistryAccess, environment);
        AttributeCommand.register(this.dispatcher, commandRegistryAccess);
        ExecuteCommand.register(this.dispatcher, commandRegistryAccess);
        BossBarCommand.register(this.dispatcher, commandRegistryAccess);
        ClearCommand.register(this.dispatcher, commandRegistryAccess);
        CloneCommand.register(this.dispatcher, commandRegistryAccess);
        DamageCommand.register(this.dispatcher, commandRegistryAccess);
        DataCommand.register(this.dispatcher);
        EffectCommand.register(this.dispatcher, commandRegistryAccess);
        EnchantCommand.register(this.dispatcher, commandRegistryAccess);
        ExperienceCommand.register(this.dispatcher);
        FillCommand.register(this.dispatcher, commandRegistryAccess);
        FillBiomeCommand.register(this.dispatcher, commandRegistryAccess);
        GameModeCommand.register(this.dispatcher);
        GiveCommand.register(this.dispatcher, commandRegistryAccess);
        ItemCommand.register(this.dispatcher, commandRegistryAccess);
        KickCommand.register(this.dispatcher);
        KillCommand.register(this.dispatcher);
        ListCommand.register(this.dispatcher);
        LootCommand.register(this.dispatcher, commandRegistryAccess);
        MessageCommand.register(this.dispatcher);
        ParticleCommand.register(this.dispatcher, commandRegistryAccess);
        PlaceCommand.register(this.dispatcher);
        PlaySoundCommand.register(this.dispatcher);
        RideCommand.register(this.dispatcher);
        RotateCommand.register(this.dispatcher);
        SeedCommand.register(this.dispatcher, environment != CommandManager.RegistrationEnvironment.INTEGRATED);
        SetBlockCommand.register(this.dispatcher, commandRegistryAccess);
        SpawnPointCommand.register(this.dispatcher);
        SpreadPlayersCommand.register(this.dispatcher);
        StopSoundCommand.register(this.dispatcher);
        SummonCommand.register(this.dispatcher, commandRegistryAccess);
        TeleportCommand.register(this.dispatcher);
        TellRawCommand.register(this.dispatcher, commandRegistryAccess);
        TimeCommand.register(this.dispatcher);
        TitleCommand.register(this.dispatcher, commandRegistryAccess);
        WeatherCommand.register(this.dispatcher);
        BanIpCommand.register(this.dispatcher);
        BanListCommand.register(this.dispatcher);
        BanCommand.register(this.dispatcher);
        DeOpCommand.register(this.dispatcher);
        OpCommand.register(this.dispatcher);
        PardonCommand.register(this.dispatcher);
        PardonIpCommand.register(this.dispatcher);
        SaveAllCommand.register(this.dispatcher);
        SaveOffCommand.register(this.dispatcher);
        SaveOnCommand.register(this.dispatcher);
        SetIdleTimeoutCommand.register(this.dispatcher);
        StopCommand.register(this.dispatcher);
        TransferCommand.register(this.dispatcher);
        WhitelistCommand.register(this.dispatcher);
    }

}
