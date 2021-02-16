package net.lightning.core.loader;

import org.bukkit.configuration.ConfigurationSection;

public interface ConfigValueAdapter<T> {

    T get(ConfigurationSection configuration, String key);

}
