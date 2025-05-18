package de.blazemcworld.fireflow.util;

import com.mojang.authlib.GameProfile;
import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.space.DummyManager;
import de.blazemcworld.fireflow.space.Space;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DummyPlayer extends ServerPlayerEntity {

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

    public DummyPlayer(Space space, int id) {
        super(FireFlow.server, space.playWorld, dummyProfiles[id - 1], SyncedClientOptions.createDefault());
        networkHandler = new ServerPlayNetworkHandler(FireFlow.server, new ClientConnection(NetworkSide.CLIENTBOUND), this, ConnectedClientData.createDefault(dummyProfiles[id - 1], false)) {
            @Override
            public void send(Packet<?> packet, @Nullable PacketCallbacks callbacks) {
                if (packet instanceof EntityVelocityUpdateS2CPacket velPacket && velPacket.getEntityId() == getId()) {
                    nextTick.add(() -> {
                        setVelocity(velPacket.getVelocityX(), velPacket.getVelocityY(), velPacket.getVelocityZ());
                    });
                }
            }
        };
        this.dummyId = id;
        this.space = space;
        this.manager = space.dummyManager;
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        manager.forgetDummy(dummyId);
        FireFlow.server.getPlayerManager().sendToAll(new PlayerRemoveS2CPacket(List.of(uuid)));
    }

    @Override
    public void tick() {
        List<Runnable> tasks = new ArrayList<>(nextTick);
        nextTick.clear();
        for (Runnable task : tasks) task.run();
        updateSupportingBlockPos(true, null);
        setOnGround(supportingBlockPos.isPresent());
        travel(Vec3d.ZERO);
        super.tick();
    }
    
    @Override
    public boolean isControlledByPlayer() {
        return false;
    }
}
