package net.lightning.api.database.model;

import com.google.common.primitives.Primitives;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.lightning.api.database.model.types.*;
import net.lightning.api.util.StringUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;

@UtilityClass
public class ModelManager {

    @Getter
    private final Map<Class<?>, String> typeAccessorsNames;
    @Getter
    private final Map<String, Function<ModelField, ModelTypeAccessor<?>>> typeAccessors;

    private final Map<Class<?>, ModelAccessor<?>> modelCache;

    static {
        typeAccessorsNames = new HashMap<>();
        typeAccessorsNames.put(String.class, "varchar");
        typeAccessorsNames.put(Integer.class, "int");
        typeAccessorsNames.put(Long.class, "long");
        typeAccessorsNames.put(Boolean.class, "bool");

        typeAccessorsNames.put(UUID.class, "uuid");
        typeAccessorsNames.put(Timestamp.class, "timestamp");

        typeAccessors = new HashMap<>();
        typeAccessors.put("varchar", VarcharTypeAccessor::new);
        typeAccessors.put("int", IntTypeAccessor::new);
        typeAccessors.put("long", LongTypeAccessor::new);
        typeAccessors.put("bool", BooleanTypeAccessor::new);

        typeAccessors.put("uuid", UUIDTypeAccessor::new);
        typeAccessors.put("timestamp", TimestampTypeAccessor::new);

        modelCache = new HashMap<>();
    }

    /**
     * Get the accessor of a model.
     *
     * @param modelClass The class of the model.
     * @param <T>        The model.
     * @return The model acessor.
     */
    @SuppressWarnings({"unchecked"})
    public <T> ModelAccessor<T> getAccessor(Class<T> modelClass) {
        if (modelCache.containsKey(modelClass)) {
            return (ModelAccessor<T>) modelCache.get(modelClass);
        }
        else {
            if (!modelClass.isAnnotationPresent(Model.class)) {
                throw new IllegalArgumentException("Trying to get the database accessor of an object which is NOT a model.");
            }

            Model options = modelClass.getAnnotation(Model.class);
            final String tableName = options.tableName().isEmpty()
                    ? StringUtil.decapitalize(modelClass.getSimpleName()).replaceAll("Model$", "").replaceAll("([A-Z])", "_$1").toLowerCase()
                    : options.tableName();

            Map<Field, ModelTypeAccessor<?>> fields = new LinkedHashMap<>();
            for (Field field : modelClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(ModelField.class)) {
                    ModelField fieldOptions = field.getAnnotation(ModelField.class);

                    String accessorTypeName = fieldOptions.type();
                    if (accessorTypeName.equals("auto")) {
                        accessorTypeName = typeAccessorsNames.get(Primitives.wrap(field.getType()));
                    }

                    ModelTypeAccessor<?> modelTypeAccessor = typeAccessors.containsKey(accessorTypeName)
                            ? typeAccessors.get(accessorTypeName).apply(fieldOptions)
                            : null;
                    if (modelTypeAccessor == null) {
                        System.err.println("Type accessor not found for'" + modelClass.getTypeName() + "#" + field.getName() + "'!");
                        continue;
                    }

                    modelTypeAccessor.updateFieldName(field);
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                        // leave it accessible for future usages
                    }
                    fields.put(field, modelTypeAccessor);
                }
            }

            Constructor<T> constructor = Arrays.stream((Constructor<T>[]) modelClass.getConstructors())
                    .filter(tConstructor -> tConstructor.getParameterCount() == fields.size())
                    .findFirst()
                    .orElse(null);
            if (constructor == null) {
                throw new IllegalStateException("Unable to find a valid constructor for model \"" + modelClass.getTypeName() + "\".");
            }

            ModelAccessor<T> modelAccessor = new ModelAccessor<>(constructor, tableName, fields);
            modelCache.put(modelClass, modelAccessor);
            return modelAccessor;
        }
    }

}
