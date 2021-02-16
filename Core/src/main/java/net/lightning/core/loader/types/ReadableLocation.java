package net.lightning.core.loader.types;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ReadableLocation extends Location {

    public ReadableLocation(@NotNull Location location) {
        this(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public ReadableLocation(@Nullable World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public ReadableLocation(@Nullable World world, double x, double y, double z, float yaw, float pitch) {
        super(world, x, y, z, yaw, pitch);
    }

}
