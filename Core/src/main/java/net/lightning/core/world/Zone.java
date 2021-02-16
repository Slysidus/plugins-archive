package net.lightning.core.world;

import org.bukkit.Location;
import org.bukkit.block.Block;

public interface Zone {

    boolean contains(int x, int y, int z);

    default boolean contains(Location location) {
        return contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    default boolean contains(Block block) {
        return contains(block.getX(), block.getY(), block.getZ());
    }

}
