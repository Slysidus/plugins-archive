package net.lightning.core.map;

import com.google.common.base.Preconditions;
import net.lightning.api.util.FileUtil;
import net.lightning.core.loader.ConfigurationModelLoadException;
import net.lightning.core.loader.ConfigurationModelLoader;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Each game has a map, no matter what the game mode is.
 * Because of this, we need convenient way of loading and sharing maps across all game modes.
 * The GameMapLoader is the key part of our system. We load .yml files to match a {@link GameMapModel}.
 */
public class GameMapLoader {

    private final ConfigurationModelLoader loader;

    public GameMapLoader() {
        this.loader = new ConfigurationModelLoader();
    }

    public <T extends GameMapModel> T loadMapModel(@NotNull File file, @NotNull Class<T> modelClass) {
        Preconditions.checkNotNull(file);

        try {
            FileUtil.checkFilePersistence(file);
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
        return loadMapModel(YamlConfiguration.loadConfiguration(file), modelClass);
    }

    public <T extends GameMapModel> T loadMapModel(@NotNull FileConfiguration configuration, @NotNull Class<T> modelClass) {
        try {
            return loader.loadSuperBuilt(configuration, modelClass, null);
        }
        catch (ConfigurationModelLoadException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

}
