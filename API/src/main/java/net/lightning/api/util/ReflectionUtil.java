package net.lightning.api.util;

import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

@UtilityClass
public class ReflectionUtil {

    public List<Method> getMethodsAnnotatedWith(Class<?> type, Class<? extends Annotation> annotation) {
        List<Method> methods = Lists.newArrayList();
        Class<?> clazz = type;
        while (clazz != Object.class) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isBridge() || method.isSynthetic())
                    continue;

                if (method.isAnnotationPresent(annotation))
                    methods.add(method);
            }
            clazz = clazz.getSuperclass();
        }
        return methods;
    }

    public List<Field> getFieldsAnnotatedWith(Class<?> type, Class<? extends Annotation> annotation) {
        List<Class<?>> classes = Lists.newLinkedList();
        Class<?> clazz = type;
        while (clazz != Object.class) {
            classes.add(clazz);
            clazz = clazz.getSuperclass();
        }
        Collections.reverse(classes);

        List<Field> fields = Lists.newLinkedList();
        for (Class<?> aClass : classes) {
            for (Field field : aClass.getDeclaredFields()) {
                if (field.isSynthetic())
                    continue;

                if (field.isAnnotationPresent(annotation))
                    fields.add(field);
            }
        }
        return fields;
    }

    public <T> void setStaticField(Field field, T value) throws IllegalAccessException, NoSuchFieldException {
        if (!field.isAccessible())
            field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        if (Modifier.isFinal(field.getModifiers())) {
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        }

        field.set(null, value);
    }

    public Field getField(Class<?> aClass, String fieldName) {
        Field field = null;
        try {
            field = aClass.getDeclaredField(fieldName);
            field.setAccessible(true);
        }
        catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return field;
    }

    public Class<?> parseType(final String className)
            throws ClassNotFoundException {
        if ("boolean".equals(className)) {
            return boolean.class;
        }
        else if ("byte".equals(className)) {
            return byte.class;
        }
        else if ("short".equals(className)) {
            return short.class;
        }
        else if ("int".equals(className)) {
            return int.class;
        }
        else if ("long".equals(className)) {
            return long.class;
        }
        else if ("float".equals(className)) {
            return float.class;
        }
        else if ("double".equals(className)) {
            return double.class;
        }
        else if ("char".equals(className)) {
            return char.class;
        }
        return Class.forName(className);
    }

}
