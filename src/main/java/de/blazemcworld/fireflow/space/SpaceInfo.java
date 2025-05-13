package de.blazemcworld.fireflow.space;

import net.minecraft.item.Item;

import java.util.Set;
import java.util.UUID;

public class SpaceInfo {

    public final int id;
    public String name;
    public Item icon;
    public UUID owner;
    public Set<UUID> developers;
    public Set<UUID> builders;

    public SpaceInfo(int id) {
        this.id = id;
    }

    public boolean isOwnerOrDeveloper(UUID uuid) {
        return owner.equals(uuid) || developers.contains(uuid);
    }

    public boolean isOwnerOrBuilder(UUID uuid) {
        return owner.equals(uuid) || builders.contains(uuid);
    }

}
