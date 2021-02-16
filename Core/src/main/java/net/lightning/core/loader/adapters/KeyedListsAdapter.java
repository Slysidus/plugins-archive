package net.lightning.core.loader.adapters;

import net.lightning.core.loader.AdvancedAdapter;
import net.lightning.core.loader.ConfigValueAdapter;
import net.lightning.core.loader.ConfigurationModelLoader;
import net.lightning.core.loader.types.KeyedList;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

public class KeyedListsAdapter extends AdvancedAdapter<KeyedList<?>> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public KeyedList<?> get(ConfigurationModelLoader modelLoader, Field field, ConfigurationSection configuration, String key) {
        ParameterizedType listType = (ParameterizedType) field.getGenericType();
        Class<?> aClass = (Class<?>) listType.getActualTypeArguments()[0];

        MemorySection configurationSection = (MemorySection) configuration.getConfigurationSection(key);
        if (configurationSection == null) {
            return new KeyedList<>();
        }

        ConfigValueAdapter<?> adapter = modelLoader.getAdapters().get(aClass);
        if (adapter == null) {
            Bukkit.getLogger().warning("No setting adapter for class '" + aClass.getCanonicalName() + "'!");
            return new KeyedList<>();
        }

        KeyedList keyedList = new KeyedList<>();
        for (String childKey : configurationSection.getKeys(false)) {
            Object value;
            if (adapter instanceof AdvancedAdapter) {
                value = ((AdvancedAdapter<?>) adapter).get(modelLoader, field, configurationSection, childKey);
            }
            else {
                value = adapter.get(configurationSection, childKey);
            }

            if (value != null)
                keyedList.add(value);
        }
        return keyedList;
    }

    @Override
    public void set(ConfigurationModelLoader modelLoader, Field field, ConfigurationSection configuration, String key, KeyedList<?> value) {
        ParameterizedType listType = (ParameterizedType) field.getGenericType();
        Class<?> aClass = (Class<?>) listType.getActualTypeArguments()[0];

        ConfigValueAdapter<?> adapter = modelLoader.getAdapters().get(aClass);
        if (adapter == null) {
            Bukkit.getLogger().warning("No setting adapter for class '" + aClass.getCanonicalName() + "'!");
            return;
        }

        for (int i = 0; i < value.size(); i++) {
            String childKey = key + "." + i;
            if (adapter instanceof AdvancedAdapter) {
                ((AdvancedAdapter<?>) adapter).pleaseJavaLetMeSetTheValue(modelLoader, null, configuration, childKey, value.get(i));
            }
            else {
                configuration.set(childKey, value.get(i));
            }
        }
    }

}
