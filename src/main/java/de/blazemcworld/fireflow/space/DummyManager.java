package de.blazemcworld.fireflow.space;

import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.util.DummyPlayer;
import de.blazemcworld.fireflow.util.Statistics;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class DummyManager {
    public final Space space;
    private final DummyPlayer[] dummies = new DummyPlayer[5];

    public DummyManager(Space space) {
        this.space = space;
    }

    public DummyPlayer getDummy(int id) {
        return dummies[id - 1];
    }

    public void spawnDummy(int id) {
        if (dummies[id - 1] != null) return;
        DummyPlayer dummy = new DummyPlayer(space, id);
        dummy.setPos(new Vec3(0, 1, 0));
        FireFlow.server.getPlayerList().broadcastAll(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(dummy)));
        space.playLevel.addFreshEntity(dummy);
        dummies[id - 1] = dummy;
        Statistics.reset(dummy);

        space.evaluator.onJoin(dummy);
    }

    public void forgetDummy(int dummyId) {
        dummies[dummyId - 1] = null;
    }

    public void reset() {
        for (int i = 0; i < dummies.length; i++) {
            DummyPlayer dummy = dummies[i];
            if (dummy == null) continue;
            dummy.discard();
            dummies[i] = null;
        }
    }
}
