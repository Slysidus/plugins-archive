package net.lightning.core.loader.adapters;

import net.lightning.core.loader.AdvancedAdapter;
import net.lightning.core.loader.ConfigurationModelLoader;
import net.lightning.core.loader.types.ReadableLocation;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Field;

public class ReadableLocationAdapter extends AdvancedAdapter<ReadableLocation> {

    @Override
    public ReadableLocation get(ConfigurationModelLoader modelLoader, Field field, ConfigurationSection configuration, String key) {
        ConfigurationSection locationSection = configuration.getConfigurationSection(key);
        if (locationSection == null) {
            return null;
        }

        String worldName = locationSection.getString("world");
        return new ReadableLocation(worldName != null ? Bukkit.getWorld(worldName) : null,
                locationSection.getDouble("x"), locationSection.getDouble("y"), locationSection.getDouble("z"),
                (float) locationSection.getDouble("yaw"), (float) locationSection.getDouble("pitch"));
    }

    @Override
    public void set(ConfigurationModelLoader modelLoader, Field field, ConfigurationSection configuration, String key, ReadableLocation value) {
        ConfigurationSection locationSection = configuration.getConfigurationSection(key);
        if (locationSection == null) {
            locationSection = configuration.createSection(key);
        }

//         TODO: excludeWorld setting and it must work with lists as well
//        if (value.getWorld() != null) {
//            locationSection.set("world", value.getWorld().getName());
//        }
        locationSection.set("x", value.getX());
        locationSection.set("y", value.getY());
        locationSection.set("z", value.getZ());
        if (value.getYaw() != 0) {
            locationSection.set("yaw", value.getYaw());
        }
        if (value.getPitch() != 0) {
            locationSection.set("pitch", value.getPitch());
        }
    }

}
