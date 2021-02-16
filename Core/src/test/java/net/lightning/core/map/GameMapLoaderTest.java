package net.lightning.core.map;

import junit.framework.TestCase;
import lombok.experimental.SuperBuilder;
import net.lightning.core.loader.ConfigurationModel;
import net.lightning.core.loader.LoaderField;
import net.lightning.core.map.annotations.FieldNotNull;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class GameMapLoaderTest extends TestCase {

    public void testLoadMapModel() {
        final String NAME = "MapTest";
        final int MAX_PLAYERS = 50;

        final String ABC = "&aABC_3";
        final double XYZ = 3.33;

        FileConfiguration fileConfiguration = new YamlConfiguration();
        fileConfiguration.set("name", NAME);
        fileConfiguration.set("max-players", MAX_PLAYERS);

        ConfigurationSection configurationSection = fileConfiguration.createSection("section-test");
        configurationSection.set("abc", ABC);
        configurationSection.set("xyz", XYZ);

        GameMapLoader mapLoader = new GameMapLoader();
        TestGameMapModel mapModel = mapLoader.loadMapModel(fileConfiguration, TestGameMapModel.class);

        assertEquals(NAME, mapModel.name);
        assertEquals(MAX_PLAYERS, mapModel.maxPlayers);
        assertEquals(ABC, mapModel.childFieldTest.abc);
        assertEquals(ChatColor.translateAlternateColorCodes('&', ABC), mapModel.childFieldTest.abcWithColors);
        assertEquals(XYZ, mapModel.childFieldTest.xyzRenamed);
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