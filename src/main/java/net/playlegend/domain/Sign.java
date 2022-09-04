package net.playlegend.domain;

import com.google.common.base.Objects;
import java.util.UUID;

public class Sign {

    private final int id;
    private final UUID owner;
    private final String world;
    private final int x;
    private final int y;
    private final int z;

    public Sign(int id, UUID owner, String world, int x, int y, int z) {
        this.id = id;
        this.owner = owner;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getId() {
        return id;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getWorld() {
        return world;
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
    public String toString() {
        return "Sign{" +
               "id=" + id +
               ", owner=" + owner +
               ", world='" + world + '\'' +
               ", x=" + x +
               ", y=" + y +
               ", z=" + z +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sign sign)) return false;
        return id == sign.id &&
               x == sign.x &&
               y == sign.y &&
               z == sign.z &&
               Objects.equal(owner, sign.owner) &&
               Objects.equal(world, sign.world);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, owner, world, x, y, z);
    }

}
