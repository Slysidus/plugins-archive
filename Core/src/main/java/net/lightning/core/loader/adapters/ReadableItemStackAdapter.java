package net.lightning.core.loader.adapters;

import net.lightning.core.loader.AdvancedAdapter;
import net.lightning.core.loader.ConfigurationModelLoader;
import net.lightning.core.loader.types.ReadableItemStack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.util.Locale;

public class ReadableItemStackAdapter extends AdvancedAdapter<ReadableItemStack> {

    @Override
    public ReadableItemStack get(ConfigurationModelLoader modelLoader, Field field, ConfigurationSection configuration, String key) {
        ConfigurationSection itemSection = configuration.getConfigurationSection(key);
        if (itemSection == null) {
            return null;
        }

        String materialName = itemSection.getString("type");
        if (materialName == null)
            materialName = itemSection.getString("material");
        materialName = materialName != null ? materialName.replaceAll("\\s", "_").replaceAll("\\W", "") : null;
        Material material;
        if (materialName == null || (material = Material.getMaterial(materialName)) == null) {
            Bukkit.getLogger().warning("Invalid item type `" + materialName + "` for key `" + key + "`!");
            return null;
        }

        ReadableItemStack itemStack = new ReadableItemStack(material,
                itemSection.getInt("amount", 1), (short) itemSection.getInt("data", itemSection.getInt("durability", 0)));

        String displayName = itemSection.getString("display-name");
        boolean enchants = false;
        if (displayName != null || (enchants = itemSection.isConfigurationSection("enchants"))) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (displayName != null) {
                itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
            }

            if (enchants) {
                ConfigurationSection enchantsSection = itemSection.getConfigurationSection("enchants");
                for (String enchantKey : enchantsSection.getKeys(false)) {
                    Enchantment enchantment = Enchantment.getByName(enchantKey.toUpperCase(Locale.ENGLISH));
                    int level = enchantsSection.getInt(enchantKey, 1);
                    itemMeta.addEnchant(enchantment, level, true);
                }
            }
            itemStack.setItemMeta(itemMeta);
        }

        return itemStack;
    }

}
