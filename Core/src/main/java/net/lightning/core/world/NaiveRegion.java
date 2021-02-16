package net.lightning.core.world;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Location;

@Getter
@ToString
@EqualsAndHashCode
public class NaiveRegion implements Zone {

    private final int minX, minZ;
    private final int maxX, maxZ;

    public NaiveRegion(Location loc1, Location loc2) {
        this(loc1.getBlockX(), loc1.getBlockZ(), loc2.getBlockX(), loc2.getBlockZ());
    }

    public NaiveRegion(int x1, int z1, int x2, int z2) {
        minX = Math.min(x1, x2);
        minZ = Math.min(z1, z2);
        maxX = Math.max(x1, x2);
        maxZ = Math.max(z1, z2);
    }

    public boolean contains(NaiveRegion region) {
        return region.getMinX() >= minX && region.getMaxX() <= maxX &&
                region.getMinZ() >= minZ && region.getMaxZ() <= maxZ;
    }

    public boolean contains(int x, int z) {
        return x >= minX && x <= maxX &&
                z >= minZ && z <= maxZ;
    }

    @Override
    public boolean contains(int x, int y, int z) {
        return contains(x, z);
    }

}
