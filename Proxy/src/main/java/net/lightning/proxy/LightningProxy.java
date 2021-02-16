package net.lightning.proxy;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Getter;
import net.lightning.api.database.MySQLHikaryDatabase;
import net.lightning.api.database.model.ModelAccessor;
import net.lightning.api.database.model.ModelManager;
import net.lightning.api.database.providers.SQLDatabaseProvider;
import net.lightning.common.CommonCacheManager;
import net.lightning.common.CommonModelDeclaration;
import net.lightning.common.models.NetworkPlayer;
import net.lightning.proxy.commands.LobbyCommand;
import net.lightning.proxy.commands.SetRankCommand;
import net.lightning.proxy.listeners.FallbackServerListener;
import net.lightning.proxy.listeners.PlayerListener;
import net.lightning.proxy.modules.LightningProxyModule;
import net.lightning.proxy.modules.friends.FriendsModule;
import net.lightning.proxy.modules.lockdown.LockdownModule;
import net.lightning.proxy.modules.moderation.ModerationModule;
import net.lightning.proxy.modules.motd.MotdModule;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.redisson.api.RedissonClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.UUID;
import java.util.function.Consumer;

@Getter
public final class LightningProxy extends Plugin {

    private SQLDatabaseProvider database;

    private ModelAccessor<NetworkPlayer>.AccessorContext playerTable;
    private CommonCacheManager commonCacheManager;

    @Override
    public void onEnable() {
        File configurationFile = new File(getDataFolder(), "config.yml");

        RedissonClient redissonClient;
        try {
            checkPersistenceDefault(configurationFile);
            Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configurationFile);

            Configuration mysqlSection = configuration.getSection("database.mysql");
            database = new MySQLHikaryDatabase(
                    mysqlSection.getString("host"),
                    mysqlSection.getInt("port"),
                    mysqlSection.getString("dbname"),
                    mysqlSection.getString("user"),
                    mysqlSection.getString("password"));
//            database.setLogQueries(true);

            Configuration redisSection = configuration.getSection("database.redis");
            redissonClient = CommonCacheManager.createClient(
                    redisSection.getString("host", "127.0.0.1"),
                    redisSection.getInt("port", 6379),
                    redisSection.getString("password"),
                    redisSection.getInt("database", 1),
                    redisSection.getBoolean("use-native-epoll"),
                    redisSection.getInt("threads", 4)
            );
        }
        catch (Exception e) {
            e.printStackTrace();
            getProxy().stop("Unable to load Lightning's configuration.");
            return;
        }

        try {
            CommonModelDeclaration.registerCustomAccessors();
            database.login();

            playerTable = ModelManager.getAccessor(NetworkPlayer.class).new AccessorContext(database);
            playerTable.createTable(true);

            commonCacheManager = CommonCacheManager.createCacheManager(redissonClient);
        }
        catch (Exception e) {
            e.printStackTrace();
            getProxy().stop("Error while initializing the database.");
            return;
        }

        getProxy().registerChannel("Lightning");

        PluginManager pluginManager = getProxy().getPluginManager();
        pluginManager.registerListener(this, new PlayerListener(this));
        pluginManager.registerListener(this, new FallbackServerListener());

        pluginManager.registerCommand(this, new SetRankCommand(this));
        pluginManager.registerCommand(this, new LobbyCommand());

        registerModules(
                new LockdownModule(this),
                new ModerationModule(this),

                new MotdModule(this),
                new FriendsModule(this)
        );
    }

    public void checkPersistenceDefault(File file) throws IOException {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        if (!file.exists()) {
            InputStream defaultConfigurationStream = getResourceAsStream(file.getName());
            Files.copy(defaultConfigurationStream, file.toPath());
        }
    }

    private void registerModules(LightningProxyModule... modules) {
        for (LightningProxyModule module : modules) {
            if (!module.register()) {
                getLogger().warning("Unable to load module \"" + module.getName() + "\"!");
            }
        }
    }

    public NetworkPlayer getPlayer(UUID uniqueId) throws Exception {
        NetworkPlayer player = commonCacheManager.getPlayerCache().get(uniqueId);
        if (player == null) {
            player = playerTable.get("unique_id", uniqueId);
        }
        return player;
    }

    @SuppressWarnings("UnstableApiUsage")
    public void sendData(ProxiedPlayer proxiedPlayer, String subChannel, Consumer<ByteArrayDataOutput> consumer) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(proxiedPlayer.getUniqueId().toString());
        out.writeUTF(subChannel);
        consumer.accept(out);
        proxiedPlayer.getServer().getInfo().sendData("Lightning", out.toByteArray());
    }

    @Override
    public void onDisable() {

    }

}
