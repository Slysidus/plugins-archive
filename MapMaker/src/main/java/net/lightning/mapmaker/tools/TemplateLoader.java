package net.lightning.mapmaker.tools;

import lombok.experimental.UtilityClass;
import net.lightning.core.loader.ConfigurationModelLoadException;
import net.lightning.core.loader.ConfigurationModelLoader;
import net.lightning.mapmaker.templates.LiveTemplate;
import net.lightning.mapmaker.templates.MapMakerMeta;
import net.lightning.mapmaker.templates.Template;
import net.lightning.mapmaker.templates.TemplateField;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

@UtilityClass
public class TemplateLoader {

    public Template parseTemplate(ConfigurationSection configurationSection)
            throws ClassNotFoundException {
        Template.TemplateBuilder templateBuilder = Template.builder();
        for (String key : configurationSection.getKeys(false)) {
            if (configurationSection.isConfigurationSection(key)) {
                templateBuilder.innerTemplate(parseTemplate(configurationSection.getConfigurationSection(key)));
            }
            else {
                TemplateField field;
                try {
                    field = TemplateField.deserialize(configurationSection.getString(key));
                    templateBuilder.field(field.getFullPath(), field);
                }
                catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                    e.printStackTrace();
                }
            }
        }
        return templateBuilder.build();
    }

    public MapMakerMeta loadMeta(FileConfiguration fileConfiguration) {
        String metaTargetTemplate = null, metaConfigFile = null, metaVersion = null;
        if (fileConfiguration.isConfigurationSection("mapmaker-meta")) {
            ConfigurationSection metaSection = fileConfiguration.getConfigurationSection("mapmaker-meta");
            metaTargetTemplate = metaSection.getString("target-template");
            metaConfigFile = metaSection.getString("config-file");
            metaVersion = metaSection.getString("version");
        }
        return new MapMakerMeta(metaTargetTemplate, metaConfigFile, metaVersion);
    }

    public void saveMeta(MapMakerMeta meta, FileConfiguration fileConfiguration) {
        ConfigurationSection metaSection = fileConfiguration.isConfigurationSection("mapmaker-meta")
                ? fileConfiguration.getConfigurationSection("mapmaker-meta")
                : fileConfiguration.createSection("mapmaker-meta");
        metaSection.set("target-template", meta.getTargetTemplate());
        metaSection.set("config-file", meta.getConfigFile());
        metaSection.set("version", meta.getVersion());
    }

    public LiveTemplate loadLiveTemplate(ConfigurationModelLoader modelLoader, Template template, FileConfiguration fileConfiguration)
            throws ConfigurationModelLoadException {
        LiveTemplate.LiveTemplateBuilder builder = LiveTemplate.builder();
        Map<String, TemplateField> templateFields = template.getFieldsRecursively();
        builder.templateFields(templateFields);

        for (String key : fileConfiguration.getKeys(true)) {
            TemplateField templateField = templateFields.get(key);
            if (templateField == null) {
                continue;
            }

            Object object = modelLoader.loadValue(templateField.getFakeField(), templateField.getCustomAdapter(), fileConfiguration, key);
            if (object == null)
                continue;
            builder.value(key, object);
        }

        builder.meta(loadMeta(fileConfiguration));
        return builder.build();
    }

}
