package net.lightning.mapmaker.templates;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import net.lightning.api.util.ReflectionUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class TemplateField {

    private final Field fakeField;

    private final String fullPath, key;

    private final boolean notNull, formatColors;

    private final int fixedLength;

    private final Class<?> customAdapter;

    public Class<?> getType() {
        return fakeField.getType();
    }

    @SneakyThrows
    public String serialize() {
        Field slotField = Field.class.getDeclaredField("slot"),
                signatureField = Field.class.getDeclaredField("signature"),
                annotationsField = Field.class.getDeclaredField("annotations");
        slotField.setAccessible(true);
        signatureField.setAccessible(true);
        annotationsField.setAccessible(true);
        String fakeFieldSerialized = String.join("|", new String[]{
                fakeField.getDeclaringClass().getTypeName(),
                fakeField.getName(),
                fakeField.getType().getTypeName(),
                String.valueOf(fakeField.getModifiers()),
                String.valueOf(slotField.get(fakeField)),
                (String) signatureField.get(fakeField),
                Arrays.toString((byte[]) annotationsField.get(fakeField)).replace(" ", "")
        });
        slotField.setAccessible(false);
        signatureField.setAccessible(false);
        annotationsField.setAccessible(false);

        return String.join(" ", new String[]{
                fakeFieldSerialized,
                fullPath,
                key,
                Boolean.toString(notNull),
                Boolean.toString(formatColors),
                Integer.toString(fixedLength),
                customAdapter.getCanonicalName()
        });
    }

    public static TemplateField deserialize(String serialized)
            throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String[] parts = serialized.split(" ");
        Preconditions.checkArgument(parts.length == 7);

        String[] fieldParts = parts[0].split("\\|");
        Constructor<?> fieldConstructor = Field.class.getDeclaredConstructors()[0];
        fieldConstructor.setAccessible(true);

        Field fakeField = (Field) fieldConstructor.newInstance(
                ReflectionUtil.parseType(fieldParts[0]),
                fieldParts[1],
                ReflectionUtil.parseType(fieldParts[2]),
                Integer.parseInt(fieldParts[3]),
                Integer.parseInt(fieldParts[4]),
                fieldParts[5].equals("null") ? null : fieldParts[5],
                fieldParts[6].equals("null") ? null : parseAnnotationsByteArray(fieldParts[6])
        );
        fieldConstructor.setAccessible(false);
        return new TemplateField(
                fakeField,
                parts[1],
                parts[2],
                parts[3].equals("true"),
                parts[4].equals("true"),
                Integer.parseInt(parts[5]),
                ReflectionUtil.parseType(parts[6]));
    }

    private static byte[] parseAnnotationsByteArray(String string) {
        List<Byte> annotationsList = Arrays.stream(string.substring(1, string.length() - 1).split(","))
                .map(Byte::parseByte)
                .collect(Collectors.toList());
        byte[] annotations = new byte[annotationsList.size()];
        for (int i = 0; i < annotationsList.size(); i++) {
            annotations[i] = annotationsList.get(i);
        }
        return annotations;
    }

}
