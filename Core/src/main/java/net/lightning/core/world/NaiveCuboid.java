package net.lightning.core.world;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.Location;

@Getter
@ToString
@EqualsAndHashCode
public class NaiveCuboid implements Zone {

    private final int minX, minY, minZ;
    private final int maxX, maxY, maxZ;

    public NaiveCuboid(Location loc1, Location loc2) {
        this(loc1.getBlockX(), loc1.getBlockY(), loc1.getBlockZ(), loc2.getBlockX(), loc2.getBlockY(), loc2.getBlockZ());
    }

    public NaiveCuboid(int x1, int y1, int z1, int x2, int y2, int z2) {
        minX = Math.min(x1, x2);
        minY = Math.min(y1, y2);
        minZ = Math.min(z1, z2);
        maxX = Math.max(x1, x2);
        maxY = Math.max(y1, y2);
        maxZ = Math.max(z1, z2);
    }

    public boolean contains(NaiveCuboid cuboid) {
        return cuboid.getMinX() >= minX && cuboid.getMaxX() <= maxX &&
                cuboid.getMinY() >= minY && cuboid.getMaxY() <= maxY &&
                cuboid.getMinZ() >= minZ && cuboid.getMaxZ() <= maxZ;
    }

    public boolean contains(int x, int y, int z) {
        return x >= minX && x <= maxX &&
                y >= minY && y <= maxY &&
                z >= minZ && z <= maxZ;
    }

}