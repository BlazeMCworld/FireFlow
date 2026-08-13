package de.blazemcworld.fireflow.code.node.impl.player.gameplay;

import com.mojang.serialization.DataResult;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.option.SoundOptions;
import de.blazemcworld.fireflow.code.type.*;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class PlaySoundNode extends Node {

    public PlaySoundNode() {
        super("play_sound", "Play Sound", "Make a player hear a sound as if it was coming from a specific position.", Items.NOTE_BLOCK);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<String> sound = new Input<>("sound", "Sound", StringType.INSTANCE).options(SoundOptions.INSTANCE);
        Input<String> mode = new Input<>("mode", "Mode", StringType.INSTANCE);
        Input<Double> volume = new Input<>("volume", "Volume", NumberType.INSTANCE);
        Input<Double> pitch = new Input<>("pitch", "Pitch", NumberType.INSTANCE);
        Input<Vec3> position = new Input<>("position", "Position", VectorType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            DataResult<Identifier> id = Identifier.read(sound.getValue(ctx));
            Optional<Holder.Reference<SoundEvent>> snd = id.isSuccess() ? BuiltInRegistries.SOUND_EVENT.get(id.getOrThrow()) : Optional.empty();
            snd.ifPresent(sndEntry -> player.getValue(ctx).tryUse(ctx, p -> {
                SoundSource category = SoundSource.MASTER;
                String modeValue = mode.getValue(ctx);

                for (SoundSource c : SoundSource.values()) {
                    if (c.getName().equalsIgnoreCase(modeValue)) {
                        category = c;
                        break;
                    }
                }

                Vec3 pos = position.getValue(ctx);
                p.connection.send(new ClientboundSoundPacket(
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

