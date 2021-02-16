package net.lightning.core.loader;

import com.google.common.collect.Maps;
import lombok.Getter;
import net.lightning.core.loader.adapters.*;
import net.lightning.core.loader.types.KeyedList;
import net.lightning.core.loader.types.ReadableBlock;
import net.lightning.core.loader.types.ReadableItemStack;
import net.lightning.core.loader.types.ReadableLocation;
import net.lightning.core.map.annotations.FieldNotNull;
import net.lightning.core.world.NaiveCuboid;
import net.lightning.core.world.NaiveRegion;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.List;
import java.util.Map;

public class ConfigurationModelLoader {

    @Getter
    private final Map<Class<?>, ConfigValueAdapter<?>> adapters = Maps.newHashMap();
    private final LoaderField defaultLoaderField;

    public ConfigurationModelLoader() {
        adapters.clear();
        adapters.put(String.class, ConfigurationSection::getString);
        adapters.put(int.class, ConfigurationSection::getInt);
        adapters.put(Integer.class, ConfigurationSection::getInt);
        adapters.put(double.class, ConfigurationSection::getDouble);
        adapters.put(Double.class, ConfigurationSection::getDouble);
        adapters.put(boolean.class, ConfigurationSection::getBoolean);
        adapters.put(Boolean.class, ConfigurationSection::getBoolean);

        /*
        Some casting
         */
        adapters.put(byte.class, ((configuration, key) -> (byte) configuration.getInt(key)));
        adapters.put(Byte.class, ((configuration, key) -> (byte) configuration.getInt(key)));
        adapters.put(short.class, ((configuration, key) -> (short) configuration.getInt(key)));
        adapters.put(Short.class, ((configuration, key) -> (short) configuration.getInt(key)));
        adapters.put(float.class, ((configuration, key) -> (float) configuration.getDouble(key)));
        adapters.put(Float.class, ((configuration, key) -> (float) configuration.getDouble(key)));

        /*
        Common objects
         */
        adapters.put(ItemStack.class, ConfigurationSection::getItemStack);
        adapters.put(Material.class, new MaterialAdapter());
        adapters.put(ChatColor.class, new ChatColorAdapter());
        adapters.put(net.md_5.bungee.api.ChatColor.class, new BungeeChatColorAdapter());

        adapters.put(Class.class, ((configuration, key) -> {
            String className = configuration.getString(key);
            if (className != null) {
                try {
                    return Class.forName(className);
                }
                catch (ClassNotFoundException ignored) {
                }
            }
            return null;
        }));
        adapters.put(List.class, new ListsAdapter());
        adapters.put(KeyedList.class, new KeyedListsAdapter());

        adapters.put(ReadableLocation.class, new ReadableLocationAdapter());
        adapters.put(ReadableBlock.class, new ReadableBlockAdapter());
        adapters.put(ReadableItemStack.class, new ReadableItemStackAdapter());
        adapters.put(NaiveRegion.class, new NaiveRegionAdapter());
        adapters.put(NaiveCuboid.class, new NaiveCuboidAdapter());

        //noinspection rawtypes
        this.defaultLoaderField = new LoaderField() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return LoaderField.class;
            }

            @Override
            public String key() {
                return "";
            }

            @Override
            public boolean formatColors() {
                return false;
            }

            @Override
            public int fixedLength() {
                return -1;
            }

            @Override
            public Class<? extends ConfigValueAdapter> customAdapter() {
                return ConfigValueAdapter.class;
            }
        };
    }

    /**
     * Loads a configuration model from a file configuration from a super built template
     * (e.i: using lombok's @SuperBuilder annotation).
     *
     * @param fileConfiguration The file to load the data from.
     * @param model             The model to load the data to.
     * @param prefix            The root configuration section.
     * @param <T>               Type of the configuration model.
     * @return Loaded configuration model.
     * @throws ConfigurationModelLoadException If the constructor isn't of an expected type or if no fields match.
     */
    public <T extends ConfigurationModel> T loadSuperBuilt(FileConfiguration fileConfiguration, Class<T> model, String prefix)
            throws ConfigurationModelLoadException, IllegalAccessException, InvocationTargetException {
        Object builderInstanceOutput = visitSuperBuilt(model, prefix, ((declaredField, field, builderInstance, key, localKey, fieldSettings) -> {
            boolean originAccessible = declaredField.isAccessible();
            if (!originAccessible) {
                declaredField.setAccessible(true);
            }

            Class<?> fieldType = field.getType();
            if (ConfigurationModel.class.isAssignableFrom(fieldType)) {
                //noinspection unchecked
                declaredField.set(builderInstance, loadSuperBuilt(fileConfiguration, (Class<T>) fieldType, key));
            }
            else {
                Object instance = loadValue(field, fieldSettings.customAdapter(), fileConfiguration, key);

                if (field.isAnnotationPresent(FieldNotNull.class) && instance == null) {
                    throw new ConfigurationModelLoadException(model, ConfigurationModelLoadException.ErrorType.NULL,
                            "Instance is null for field \"" + field.getName() + "\" (" + key + ") while marked as @FieldNotNull.");
                }

                if (instance != null) {
                    if (fieldType.equals(String.class) && fieldSettings.formatColors()) {
                        instance = ChatColor.translateAlternateColorCodes('&', (String) instance);
                    }
                }

                declaredField.set(builderInstance, instance);
            }

            if (!originAccessible) {
                declaredField.setAccessible(false);
            }
        }));

        Method buildBuilderMethod;
        try {
            buildBuilderMethod = model.getDeclaredConstructors()[0].getParameterTypes()[0].getMethod("build");
        }
        catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
        Object instance = buildBuilderMethod.invoke(builderInstanceOutput);
        if (instance.getClass().equals(model)) {
            //noinspection unchecked
            return (T) instance;
        }
        throw new UnsupportedOperationException();
    }

    public <T extends ConfigurationModel> Object visitSuperBuilt(Class<T> model, String prefix, SuperBuiltVisitor visitor)
            throws ConfigurationModelLoadException, InvocationTargetException, IllegalAccessException {
        if (model.getDeclaredConstructors().length != 1) {
            throw new ConfigurationModelLoadException(model, ConfigurationModelLoadException.ErrorType.BAD_CONSTRUCTOR,
                    "Configuration model must have one constructor( only).");
        }

        Constructor<?> constructor = model.getDeclaredConstructors()[0];
        if (constructor.getParameterCount() != 1) {
            throw new ConfigurationModelLoadException(model, ConfigurationModelLoadException.ErrorType.BAD_CONSTRUCTOR,
                    "Configuration model's constructor must take one parameter only: the super builder.");
        }
        Class<?> builderType = constructor.getParameterTypes()[0];

        Method createBuilderMethod;
        try {
            createBuilderMethod = model.getMethod("builder");
            if (!Modifier.isStatic(createBuilderMethod.getModifiers())) {
                throw new ConfigurationModelLoadException(model, ConfigurationModelLoadException.ErrorType.BAD_BUILDER_METHOD,
                        "Configuration model's builder() is not a static method.");
            }
            if (createBuilderMethod.getParameterCount() != 0) {
                throw new ConfigurationModelLoadException(model, ConfigurationModelLoadException.ErrorType.BAD_BUILDER_METHOD,
                        "Configuration model's builder() must have no parameter.");
            }
            if (!createBuilderMethod.getReturnType().equals(builderType)) {
                throw new ConfigurationModelLoadException(model, ConfigurationModelLoadException.ErrorType.BAD_BUILDER_METHOD,
                        "Configuration model's builder() mismatches required type by constructor.");
            }

        }
        catch (NoSuchMethodException e) {
            throw new ConfigurationModelLoadException(model, ConfigurationModelLoadException.ErrorType.BAD_BUILDER_METHOD,
                    "Configuration model is missing a builder() method to get a new instance of the super builder.");
        }

        boolean keepVisiting = true;
        Class<?> visitingModelClass = model, visitingBuilderClass = builderType;
        Object builderInstance = createBuilderMethod.invoke(null);

        while (keepVisiting) {
            for (Field declaredField : visitingBuilderClass.getDeclaredFields()) {
                Field field;
                try {
                    field = visitingModelClass.getDeclaredField(declaredField.getName());
                    if (!field.getType().equals(declaredField.getType())) {
                        throw new ConfigurationModelLoadException(model, ConfigurationModelLoadException.ErrorType.UNEXPECTED_BEHAVIOR,
                                "Error while visiting model supers and its builders: field \"" + declaredField.getName() + "\" from '" +
                                        visitingBuilderClass.getCanonicalName() + "' does not have the same type in '" + visitingModelClass.getCanonicalName() + "'");
                    }
                }
                catch (NoSuchFieldException e) {
                    throw new ConfigurationModelLoadException(model, ConfigurationModelLoadException.ErrorType.UNKNOWN_FIELD,
                            "Error while visiting model supers and its builders: field \"" + declaredField.getName() + "\" from '" +
                                    visitingBuilderClass.getCanonicalName() + "' does not exist in '" + visitingModelClass.getCanonicalName() + "'");
                }

                final LoaderField fieldSettings = field.isAnnotationPresent(LoaderField.class)
                        ? field.getAnnotation(LoaderField.class)
                        : defaultLoaderField;

                final String localKey = (fieldSettings.key().isEmpty() ? toConfigKey(field) : fieldSettings.key());
                final String key = (prefix != null ? prefix + "." : "") + localKey;

                visitor.visit(declaredField, field, builderInstance, key, localKey, fieldSettings);
            }

            boolean keepVisitingBuilder = visitingBuilderClass.getSuperclass() != null
                    && !visitingBuilderClass.getSuperclass().equals(Object.class);
            boolean keepVisitingModel = visitingModelClass.getSuperclass() != null
                    && !visitingModelClass.getSuperclass().equals(Object.class);
            if (keepVisitingBuilder && !keepVisitingModel) {
                throw new ConfigurationModelLoadException(model, ConfigurationModelLoadException.ErrorType.UNEXPECTED_BEHAVIOR,
                        "Builder still extends a super builder while model class doesn't anymore.");
            }
            else if (!keepVisitingBuilder) {
                keepVisiting = false;
            }
            else {
                visitingModelClass = visitingModelClass.getSuperclass();
                visitingBuilderClass = visitingBuilderClass.getSuperclass();
            }
        }
        return builderInstance;
    }

    public Object loadValue(Field field, Class<?> customAdapter, FileConfiguration fileConfiguration, String key)
            throws ConfigurationModelLoadException {
        if (field == null) {
            return null;
        }

        ConfigValueAdapter<?> adapter = adapters.get(field.getType());
        if (customAdapter != null && !customAdapter.equals(ConfigValueAdapter.class)) {
            try {
                adapter = (ConfigValueAdapter<?>) customAdapter.newInstance();
            }
            catch (InstantiationException | IllegalAccessException e) {
                throw new ConfigurationModelLoadException(null, ConfigurationModelLoadException.ErrorType.INVALID_ADAPTER,
                        "Adapter of type '" + customAdapter.getCanonicalName() + "' cannot be instantiated.");
            }
        }

        Object instance = fileConfiguration.get(key, null);
        if (adapter != null && instance != null) {
            if (adapter instanceof AdvancedAdapter) {
                instance = ((AdvancedAdapter<?>) adapter).get(this, field, fileConfiguration, key);
            }
            else {
                instance = adapter.get(fileConfiguration, key);
            }
        }
        return instance;
    }

    /**
     * Gets a configuration key from a field's name (giving a field object).
     *
     * @param field Field with camelCase name.
     * @return Field's kebab-case name.
     */
    private String toConfigKey(Field field) {
        return field.getName().replaceAll("([A-Z])", "-$1").toLowerCase();
    }

    public interface SuperBuiltVisitor {

        void visit(Field declaredField, Field field, Object builderInstance, String key, String localKey, LoaderField fieldSettings)
                throws IllegalAccessException, ConfigurationModelLoadException, InvocationTargetException;

    }

}
