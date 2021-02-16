package net.lightning.core.loader;

import junit.framework.TestCase;
import lombok.experimental.SuperBuilder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.lang.reflect.InvocationTargetException;

public class ConfigurationModelLoaderTest extends TestCase {

    public void testLoad()
            throws InvocationTargetException, ConfigurationModelLoadException, IllegalAccessException {
        final String NAME = "TestName";
        final int NUMBER = 50;

        FileConfiguration fileConfiguration = new YamlConfiguration();
        fileConfiguration.set("custom-name", NAME);
        fileConfiguration.set("number", NUMBER);

        ConfigurationModelLoader modelLoader = new ConfigurationModelLoader();
        TestModelImpl testModel = modelLoader.loadSuperBuilt(fileConfiguration, TestModelImpl.class, null);
        assertEquals(NAME, testModel.name);
        assertEquals(NUMBER, testModel.number);
    }

    @SuperBuilder
    public static class TestModelImpl extends ConfigurationModel {

        public final @LoaderField(key = "custom-name") String name;
        public final int number;

    }

}