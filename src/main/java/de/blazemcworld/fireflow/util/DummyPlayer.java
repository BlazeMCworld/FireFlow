package de.blazemcworld.fireflow.util;

import com.mojang.authlib.GameProfile;
import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.space.DummyManager;
import de.blazemcworld.fireflow.space.Space;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DummyPlayer extends ServerPlayer {

    private static final GameProfile[] dummyProfiles = {
            new GameProfile(UUID.fromString("a1a17bc1-912d-42f6-81de-18cdb9a482eb"), "Dummy-1"),
            new GameProfile(UUID.fromString("b2b249fa-9cb6-476a-8e81-8e427a4a37cf"), "Dummy-2"),
            new GameProfile(UUID.fromString("c3c3342c-7883-4cdf-bf22-0882d910edc5"), "Dummy-3"),
            new GameProfile(UUID.fromString("d4d4be72-df27-4889-836e-903cc6e14436"), "Dummy-4"),
            new GameProfile(UUID.fromString("e5e5bfb2-85a5-44e4-b70b-84e60450ed74"), "Dummy-5")
    };

    public final int dummyId;
    public final Space space;
    public final DummyManager manager;
    private final List<Runnable> nextTick = new ArrayList<>();
    public boolean exitCalled = false;

    public DummyPlayer(Space space, int id) {
        super(FireFlow.server, space.playLevel, dummyProfiles[id - 1], ClientInformation.createDefault());
        connection = new ServerGamePacketListenerImpl(FireFlow.server, new net.minecraft.network.Connection(PacketFlow.CLIENTBOUND), this, CommonListenerCookie.createInitial(dummyProfiles[id - 1], false)) {

            @Override
            public void send(@NonNull Packet<?> packet, @org.jspecify.annotations.Nullable ChannelFutureListener listener) {
                if (packet instanceof ClientboundSetEntityMotionPacket(
                        int id1, net.minecraft.world.phys.Vec3 movement
                ) && id1 == getId()) {
                    nextTick.add(() -> setDeltaMovement(movement.x, movement.y, movement.z));
                }
            }
        };
        this.dummyId = id;
        this.space = space;
        this.manager = space.dummyManager;
    }

    @Override
    public void remove(@NonNull RemovalReason reason) {
        if (!exitCalled) {
            exitCalled = true;
            space.evaluator.exitPlay(this);
        }
        super.remove(reason);
        manager.forgetDummy(dummyId);
        FireFlow.server.getPlayerList().broadcastAll(new ClientboundPlayerInfoRemovePacket(List.of(uuid)));
    }

    @Override
    public void tick() {
        List<Runnable> tasks = new ArrayList<>(nextTick);
        nextTick.clear();
        for (Runnable task : tasks) task.run();
        checkSupportingBlock(true, null);
        setOnGround(mainSupportingBlockPos.isPresent());
        super.tick();
        super.doTick();
    }

    @Override
    public void doTick() {
        // Moved into regular tick
    }

    @Override
    public boolean isClientAuthoritative() {
        return false;
    }
}
