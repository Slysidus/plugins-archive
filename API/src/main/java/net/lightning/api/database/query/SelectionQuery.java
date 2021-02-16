package net.lightning.api.database.query;

import com.google.common.primitives.Primitives;
import lombok.Getter;
import net.lightning.api.database.model.ModelAccessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class SelectionQuery {

    private final Map<String, Object> where;

    public SelectionQuery() {
        this.where = new HashMap<>();
    }

    public SelectionQuery where(String column, Object value) {
        where.put(column, value);
        return this;
    }

    public void validate(ModelAccessor<?> modelAccessor) {
        Set<String> fieldNames = modelAccessor.getFieldsByName().keySet();
        List<String> unknownFields = where.keySet().stream()
                .filter(field -> !fieldNames.contains(field))
                .collect(Collectors.toList());
        if (!unknownFields.isEmpty()) {
            throw new IllegalArgumentException("Fields '" + String.join("', '", unknownFields) + "' are not valid fields. [INVALID SELECTION QUERY]");
        }

        for (Map.Entry<String, Object> entry : where.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }

            Class<?> queryFieldType = Primitives.wrap(entry.getValue().getClass());
            Class<?> modelFieldType = Primitives.wrap(modelAccessor.getFieldsByName().get(entry.getKey()).getType());
            if (!queryFieldType.equals(modelFieldType)) {
                throw new IllegalArgumentException("Field '" + entry.getKey() + "' doesn't match the model type. " +
                        "Expected \"" + modelFieldType.getTypeName() + "\" but got \"" + queryFieldType.getTypeName() + "\" instead [INVALID SELECTION QUERY]");
            }
        }
    }

}
