package net.lightning.core.loader.adapters;

import net.lightning.core.loader.ConfigValueAdapter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;

public class BungeeChatColorAdapter implements ConfigValueAdapter<ChatColor> {

    @Override
    public ChatColor get(ConfigurationSection configuration, String key) {
        String colorName = configuration.getString(key);
        if (colorName == null)
            return null;

        String finalColorName = colorName.replace(' ', '_').trim();
        return Arrays.stream(ChatColor.values())
                .filter(color -> color.name().equalsIgnoreCase(finalColorName))
                .findFirst()
                .orElse(ChatColor.RESET);
    }

}
