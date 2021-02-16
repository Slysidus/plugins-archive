package net.lightning.core.loader.adapters;

import net.lightning.core.loader.ConfigValueAdapter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public class MaterialAdapter implements ConfigValueAdapter<Material> {

    @Override
    public Material get(ConfigurationSection configuration, String key) {
        String materialName = configuration.getString(key);
        if (materialName == null)
            return null;

        return Material.getMaterial(materialName.toUpperCase());
    }

}
