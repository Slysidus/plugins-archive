package net.lightning.core;

import lombok.Getter;
import net.lightning.common.CommonCacheManager;
import net.lightning.common.CommonModelDeclaration;
import net.lightning.common.Rank;
import net.lightning.core.channel.LightningChannelListener;
import net.lightning.core.channel.NicknameAPI;
import net.lightning.core.commands.CoreCommand;
import net.lightning.core.listeners.GameJoinAndQuitListener;
import net.lightning.core.listeners.InventoryListener;
import net.lightning.core.listeners.NativePlayerListener;
import net.lightning.core.server.CraftServerHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

@Getter
public abstract class LightningGamePlugin<GameObject extends Game> extends JavaPlugin {

    private boolean library;

    private CraftServerHandler craftServerHandler;
    private NicknameAPI nicknameAPI;

    private CommonCacheManager commonCacheManager;

    protected GameObject game;

    @Override
    public final void onEnable() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        if (pluginManager.isPluginEnabled("MapMaker")) {
            getLogger().info("MapMaker plugin detected. Acting as a library.");
            library = true;
            return;
        }

        try {
            craftServerHandler = new CraftServerHandler(this);

            File libsDir = new File(getDataFolder(), "libs");
            if (libsDir.exists() && libsDir.isDirectory()) {
                for (File file : libsDir.listFiles()) {
                    if (file.getName().endsWith(".jar")) {
                        craftServerHandler.injectClasspath(file);
                    }
                }
            }
        }
        catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchFieldException | NoSuchMethodException ex) {
            ex.printStackTrace();
            Bukkit.shutdown();
        }
        nicknameAPI = new NicknameAPI(this);

        try {
            saveDefaultConfig();
            ConfigurationSection redisSection = getConfig().getConfigurationSection("database.redis");
            CommonModelDeclaration.registerCustomAccessors();
            commonCacheManager = CommonCacheManager.createCacheManager(CommonCacheManager.createClient(
                    redisSection.getString("host", "127.0.0.1"),
                    redisSection.getInt("port", 6379),
                    redisSection.getString("password"),
                    redisSection.getInt("database", 1),
                    redisSection.getBoolean("use-native-epoll"),
                    redisSection.getInt("threads", 4)));
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Bukkit.shutdown();
        }

        getServer().getMessenger().registerIncomingPluginChannel(this, "Lightning", new LightningChannelListener(this));

        pluginManager.registerEvents(new GameJoinAndQuitListener(this), this);
        pluginManager.registerEvents(new NativePlayerListener(this), this);

        this.game = initGame();
        pluginManager.registerEvents(new InventoryListener(game), this);

        postInit();
        GameNPECatcher.validateGameState(game);
    }

    @Override
    public final void onDisable() {
        if (library) {
            return;
        }

        destroyGame();
    }

    public abstract GameObject initGame();

    public void postInit() {
    }

    public abstract void destroyGame();

    public void onPlayerRankUpdate(GamePlayer gamePlayer, Rank previousRank) {
    }

    protected void registerCommands(CoreCommand... commands) {
        for (CoreCommand command : commands) {
            command.registerSelf();
        }
    }

}
