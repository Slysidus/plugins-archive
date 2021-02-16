package net.lightning.mapmaker.templates;

import com.google.common.collect.Maps;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class Template {

    @Singular
    private final List<Template> innerTemplates;

    @Singular
    private final Map<String, TemplateField> fields;

    public Map<String, TemplateField> getFieldsRecursively() {
        Map<String, TemplateField> fieldMap = Maps.newLinkedHashMap(fields);
        for (Template innerTemplate : innerTemplates) {
            fieldMap.putAll(innerTemplate.getFieldsRecursively());
        }
        return fieldMap;
    }

}
