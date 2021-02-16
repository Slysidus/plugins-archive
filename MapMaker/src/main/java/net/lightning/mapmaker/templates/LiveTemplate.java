package net.lightning.mapmaker.templates;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class LiveTemplate {

    /**
     * Full-key to object map for values. Values here are type-checked and null-checked.
     */
    private final Map<String, Object> values;

    /**
     * Full-key to template field.
     */
    private final Map<String, TemplateField> templateFields;

    private final MapMakerMeta meta;

    @Builder
    public LiveTemplate(@Singular Map<String, Object> values,
                        @Singular Map<String, TemplateField> templateFields, MapMakerMeta meta) {
        this.values = Maps.newHashMap(values);
        this.templateFields = templateFields;
        this.meta = meta;
    }

    public List<Map.Entry<TemplateField, Object>> getSetValues() {
        return values.keySet().stream()
                .map(key -> Maps.immutableEntry(templateFields.get(key), values.get(key)))
                .collect(Collectors.toList());
    }

    public List<TemplateField> getUnsetValues() {
        Set<String> valuesSet = Sets.newHashSet(values.keySet());
        Set<String> templateKeys = Sets.newHashSet(templateFields.keySet());
        templateKeys.removeAll(valuesSet);
        return templateKeys.stream()
                .map(templateFields::get)
                .collect(Collectors.toList());
    }

    public List<TemplateField> getInvalidSize() {
        return getSetValues().stream()
                .filter(entry -> Collection.class.isAssignableFrom(entry.getKey().getType()))
                .filter(entry -> entry.getValue() != null && ((Collection<?>) entry.getValue()).size() != entry.getKey().getFixedLength())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

}
