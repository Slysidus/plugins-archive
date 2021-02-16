package net.lightning.core.loader.adapters;

import net.lightning.core.loader.AdvancedAdapter;
import net.lightning.core.loader.ConfigurationModelLoader;
import net.lightning.core.world.NaiveCuboid;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Field;

public class NaiveCuboidAdapter extends AdvancedAdapter<NaiveCuboid> {

    @Override
    public NaiveCuboid get(ConfigurationModelLoader modelLoader, Field field, ConfigurationSection configuration, String key) {
        ConfigurationSection section = configuration.getConfigurationSection(key);
        if (section == null) {
            return null;
        }

        return new NaiveCuboid(
                section.getInt("minX"), section.getInt("minY"), section.getInt("minZ"),
                section.getInt("maxX"), section.getInt("maxY"), section.getInt("maxZ"));
    }

    @Override
    public void set(ConfigurationModelLoader modelLoader, Field field, ConfigurationSection configuration, String key, NaiveCuboid value) {
        ConfigurationSection section = configuration.getConfigurationSection(key);
        if (section == null) {
            section = configuration.createSection(key);
        }

        section.set("minX", value.getMinX());
        section.set("minY", value.getMinY());
        section.set("minZ", value.getMinZ());
        section.set("maxX", value.getMaxX());
        section.set("maxY", value.getMaxY());
        section.set("maxZ", value.getMaxZ());
    }

}
