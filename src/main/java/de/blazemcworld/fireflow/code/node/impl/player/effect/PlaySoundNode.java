package de.blazemcworld.fireflow.code.node.impl.player.effect;

import com.mojang.serialization.DataResult;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.*;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class PlaySoundNode extends Node {

    public PlaySoundNode() {
        super("play_sound", "Play Sound", "Make a player hear a sound as if it was coming from a specific position.", Items.NOTE_BLOCK);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<String> sound = new Input<>("sound", "Sound", StringType.INSTANCE);
        Input<String> mode = new Input<>("mode", "Mode", StringType.INSTANCE);
        Input<Double> volume = new Input<>("volume", "Volume", NumberType.INSTANCE);
        Input<Double> pitch = new Input<>("pitch", "Pitch", NumberType.INSTANCE);
        Input<Vec3d> position = new Input<>("position", "Position", VectorType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            DataResult<Identifier> id = Identifier.validate(sound.getValue(ctx));
            Optional<RegistryEntry.Reference<SoundEvent>> snd = id.isSuccess() ? Registries.SOUND_EVENT.getEntry(Identifier.of(sound.getValue(ctx))) : Optional.empty();
            Vec3d pos = position.getValue(ctx);
            snd.ifPresent(sndEntry -> player.getValue(ctx).tryUse(ctx, p -> {
                SoundCategory category = SoundCategory.MASTER;
                String modeValue = mode.getValue(ctx);

                for (SoundCategory c : SoundCategory.values()) {
                    if (c.getName().equalsIgnoreCase(modeValue)) {
                        category = c;
                        break;
                    }
                }

                p.networkHandler.sendPacket(new PlaySoundS2CPacket(
                        sndEntry, category, pos.x, pos.y, pos.z,
                        volume.getValue(ctx).floatValue(), pitch.getValue(ctx).floatValue(),
                        p.getRandom().nextInt()
                ));
            }));
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new PlaySoundNode();
    }
}

