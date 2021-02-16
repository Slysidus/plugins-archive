package net.lightning.mapmaker.tools;

import lombok.experimental.UtilityClass;
import net.lightning.core.loader.ConfigurationModel;
import net.lightning.core.loader.ConfigurationModelLoadException;
import net.lightning.core.loader.ConfigurationModelLoader;
import net.lightning.core.map.GameMapModel;
import net.lightning.core.map.annotations.FieldNotNull;
import net.lightning.mapmaker.templates.Template;
import net.lightning.mapmaker.templates.TemplateField;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.lang.reflect.InvocationTargetException;

@UtilityClass
public class TemplateGenerator {

    public FileConfiguration generateTemplate(Class<? extends GameMapModel> mapModelClass)
            throws IllegalAccessException, ConfigurationModelLoadException, InvocationTargetException {
        FileConfiguration templateOutput = new YamlConfiguration();

        ConfigurationModelLoader modelLoader = new ConfigurationModelLoader();
        Template template = getTemplate(modelLoader, mapModelClass, null);
        writeTemplate(template, templateOutput);

        return templateOutput;
    }

    private void writeTemplate(Template template, FileConfiguration templateOutput) {
        template.getFields().forEach((path, field) -> templateOutput.set(path, field.serialize()));
        for (Template innerTemplate : template.getInnerTemplates()) {
            writeTemplate(innerTemplate, templateOutput);
        }
    }

    private Template getTemplate(ConfigurationModelLoader modelLoader, Class<? extends ConfigurationModel> model, String prefix)
            throws ConfigurationModelLoadException, IllegalAccessException, InvocationTargetException {
        Template.TemplateBuilder templateBuilder = Template.builder();

        modelLoader.visitSuperBuilt(model, prefix, ((declaredField, field, builderInstance, key, localKey, fieldSettings) -> {
            if (ConfigurationModel.class.isAssignableFrom(field.getType())) {
                //noinspection unchecked
                templateBuilder.innerTemplate(getTemplate(modelLoader, (Class<? extends ConfigurationModel>) field.getType(), key));
            }
            else {
                templateBuilder.field(key, new TemplateField(
                        field,
                        key,
                        localKey,
                        field.isAnnotationPresent(FieldNotNull.class) || field.getType().isPrimitive() || fieldSettings.fixedLength() > -1,
                        fieldSettings.formatColors(),
                        fieldSettings.fixedLength(),
                        fieldSettings.customAdapter()));
            }
        }));
        return templateBuilder.build();
    }

}
