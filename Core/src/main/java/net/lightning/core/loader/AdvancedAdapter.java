package net.lightning.core.loader;

import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Field;

public abstract class AdvancedAdapter<T> implements ConfigValueAdapter<T> {

    public abstract T get(ConfigurationModelLoader modelLoader, Field field, ConfigurationSection configuration, String key);

    public void set(ConfigurationModelLoader modelLoader, Field field, ConfigurationSection configuration, String key, T value) {
        configuration.set(key, value);
    }

    @SuppressWarnings("unchecked")
    public final void pleaseJavaLetMeSetTheValue(ConfigurationModelLoader modelLoader, Field field, ConfigurationSection configuration, String key, Object value) {
        set(modelLoader, field, configuration, key, (T) value);
    }

    @Override
    public final T get(ConfigurationSection configuration, String key) {
        throw new UnsupportedOperationException();
    }

}
