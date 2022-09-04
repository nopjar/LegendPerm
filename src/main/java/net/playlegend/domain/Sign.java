package net.playlegend.domain;

import java.util.Objects;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

public final class Sign {

    private final int id;
    private final UUID owner;
    private final String worldName;
    private final int x;
    private final int y;
    private final int z;
    private String groupName;

    public Sign(int id, UUID owner, String groupName, String worldName, int x, int y, int z) {
        this.id = id;
        this.owner = owner;
        this.groupName = groupName;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Nullable
    public Location getAsBukkitLocation() {
        World world = Bukkit.getWorld(this.worldName);
        if (world == null) return null;

        return new Location(world, x, y, z);
    }

    public int getId() {
        return id;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getWorldName() {
        return worldName;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Sign) obj;
        return this.id == that.id &&
               Objects.equals(this.owner, that.owner) &&
               Objects.equals(this.groupName, that.groupName) &&
               Objects.equals(this.worldName, that.worldName) &&
               this.x == that.x &&
               this.y == that.y &&
               this.z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, owner, groupName, worldName, x, y, z);
    }

    @Override
    public String toString() {
        return "Sign[" +
               "id=" + id + ", " +
               "owner=" + owner + ", " +
               "groupName=" + groupName + ", " +
               "worldName=" + worldName + ", " +
               "x=" + x + ", " +
               "y=" + y + ", " +
               "z=" + z + ']';
    }


}
