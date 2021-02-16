package net.lightning.core.loader.adapters;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import net.lightning.core.loader.AdvancedAdapter;
import net.lightning.core.loader.ConfigValueAdapter;
import net.lightning.core.loader.ConfigurationModelLoader;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListsAdapter extends AdvancedAdapter<List<?>> {

    @Getter
    private final Map<Class<?>, ConfigValueAdapter<List<?>>> listAdapters = Maps.newHashMap();

    public ListsAdapter() {
        listAdapters.clear();
        listAdapters.put(String.class, ConfigurationSection::getStringList);
        listAdapters.put(Integer.class, ConfigurationSection::getIntegerList);
        listAdapters.put(Double.class, ConfigurationSection::getDoubleList);
        listAdapters.put(Boolean.class, ConfigurationSection::getBooleanList);

        listAdapters.put(Material.class, new AnyListAdapter<Material>() {
            @Override
            Material serialize(String s) {
                return Material.getMaterial(s.toUpperCase());
            }
        });
    }

    @Override
    public List<?> get(ConfigurationModelLoader modelLoader, Field field, ConfigurationSection configuration, String key) {
        ParameterizedType listType = (ParameterizedType) field.getGenericType();
        Class<?> aClass = (Class<?>) listType.getActualTypeArguments()[0];

        ConfigValueAdapter<List<?>> listAdapter = listAdapters.get(aClass);
        if (listAdapter != null) {
            return listAdapter.get(configuration, key);
        }
        else {
            try {
                List<?> genericList = ((List<?>) configuration.get(key));
                if (genericList != null) {
                    if (genericList.isEmpty())
                        return Lists.newArrayList();
                    else if (genericList.get(0).getClass().equals(aClass))
                        return genericList;
                }
            }
            catch (ClassCastException ignored) {
            }
        }
        return null;
    }

    private static abstract class AnyListAdapter<T> implements ConfigValueAdapter<List<?>> {

        @Override
        public List<T> get(ConfigurationSection configuration, String key) {
            ArrayList<T> list = Lists.newArrayList();
            for (String stringValue : configuration.getStringList(key)) {
                if (stringValue == null)
                    continue;
                list.add(serialize(stringValue));
            }
            return list;
        }

        abstract T serialize(String s);

    }

}
