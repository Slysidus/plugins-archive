package net.lightning.core.loader.types;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ReadableBlock extends ReadableLocation {

    public ReadableBlock(@NotNull Location location) {
        this(location.getWorld(), location.getX(), location.getY(), location.getZ());
    }

    public ReadableBlock(@Nullable World world, double x, double y, double z) {
        super(world, x, y, z);
    }

}
