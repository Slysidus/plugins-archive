package net.lightning.mapmaker.tools;

import junit.framework.TestCase;
import lombok.experimental.SuperBuilder;
import net.lightning.core.loader.ConfigurationModel;
import net.lightning.core.loader.ConfigurationModelLoadException;
import net.lightning.core.loader.ConfigurationModelLoader;
import net.lightning.core.loader.LoaderField;
import net.lightning.core.map.GameMapModel;
import net.lightning.core.map.annotations.FieldNotNull;
import net.lightning.mapmaker.templates.LiveTemplate;
import net.lightning.mapmaker.templates.Template;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.lang.reflect.InvocationTargetException;

public class TemplateGeneratorTest extends TestCase {

    public void testGenerateTemplate()
            throws IllegalAccessException, ConfigurationModelLoadException, InvocationTargetException, ClassNotFoundException {
        FileConfiguration fileConfiguration = TemplateGenerator.generateTemplate(TestGameMapModel.class);
        Template template = TemplateLoader.parseTemplate(fileConfiguration);

        FileConfiguration preloadedValues = new YamlConfiguration();
        preloadedValues.set("name", "random-name");
        preloadedValues.set("section-test.abc", "abc");
        LiveTemplate liveTemplate = TemplateLoader.loadLiveTemplate(new ConfigurationModelLoader(), template, preloadedValues);

        assertEquals(2, liveTemplate.getSetValues().size());
    }

    @SuperBuilder
    public static class TestGameMapModel extends GameMapModel {

        @FieldNotNull
        @LoaderField(key = "section-test")
        private final ChildFieldClass childFieldTest;

        @SuperBuilder
        public static class ChildFieldClass extends ConfigurationModel {

            private final String abc;

            @LoaderField(key = "abc", formatColors = true)
            private final String abcWithColors;

            @LoaderField(key = "xyz")
            private final double xyzRenamed;

        }

    }

}