package net.lightning.core.loader.adapters;

import net.lightning.core.loader.AdvancedAdapter;
import net.lightning.core.loader.ConfigurationModelLoader;
import net.lightning.core.world.NaiveRegion;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Field;

public class NaiveRegionAdapter extends AdvancedAdapter<NaiveRegion> {

    @Override
    public NaiveRegion get(ConfigurationModelLoader modelLoader, Field field, ConfigurationSection configuration, String key) {
        ConfigurationSection section = configuration.getConfigurationSection(key);
        if (section == null) {
            return null;
        }

        return new NaiveRegion(
                section.getInt("minX"), section.getInt("minZ"),
                section.getInt("maxX"), section.getInt("maxZ"));
    }

    @Override
    public void set(ConfigurationModelLoader modelLoader, Field field, ConfigurationSection configuration, String key, NaiveRegion value) {
        ConfigurationSection section = configuration.getConfigurationSection(key);
        if (section == null) {
            section = configuration.createSection(key);
        }

        section.set("minX", value.getMinX());
        section.set("minZ", value.getMinZ());
        section.set("maxX", value.getMaxX());
        section.set("maxZ", value.getMaxZ());
    }

}
