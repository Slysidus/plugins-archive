package net.lightning.core.loader.adapters;

import net.lightning.core.loader.AdvancedAdapter;
import net.lightning.core.loader.ConfigurationModelLoader;
import net.lightning.core.loader.types.ReadableBlock;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Field;

public class ReadableBlockAdapter extends AdvancedAdapter<ReadableBlock> {

    @Override
    public ReadableBlock get(ConfigurationModelLoader modelLoader, Field field, ConfigurationSection configuration, String key) {
        ConfigurationSection locationSection = configuration.getConfigurationSection(key);
        if (locationSection == null) {
            return null;
        }

        String worldName = locationSection.getString("world");
        return new ReadableBlock(worldName != null ? Bukkit.getWorld(worldName) : null,
                locationSection.getDouble("x"), locationSection.getDouble("y"), locationSection.getDouble("z"));
    }

    @Override
    public void set(ConfigurationModelLoader modelLoader, Field field, ConfigurationSection configuration, String key, ReadableBlock value) {
        ConfigurationSection locationSection = configuration.getConfigurationSection(key);
        if (locationSection == null) {
            locationSection = configuration.createSection(key);
        }

        locationSection.set("x", value.getX());
        locationSection.set("y", value.getY());
        locationSection.set("z", value.getZ());
    }

}
