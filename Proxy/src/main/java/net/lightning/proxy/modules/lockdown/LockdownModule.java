package net.lightning.proxy.modules.lockdown;

import lombok.Getter;
import lombok.Setter;
import net.lightning.api.util.FileUtil;
import net.lightning.proxy.LightningProxy;
import net.lightning.proxy.modules.LightningProxyModule;
import net.lightning.proxy.util.MotdUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
public class LockdownModule extends LightningProxyModule {

    private final String name = "Lockdown";

    private final File lockdownFile;

    private boolean enabled;
    private String message, centeredMotdMessage;
    private Set<String> trusted;

    public LockdownModule(LightningProxy plugin) {
        super(plugin);
        this.lockdownFile = new File(plugin.getDataFolder(), "lockdown.yml");

        this.enabled = false;
        this.message = null;
        this.trusted = new HashSet<>();
    }

    @Override
    public boolean register() {
        try {
            FileUtil.checkFilePersistence(lockdownFile);
            Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(lockdownFile);
            this.enabled = configuration.getBoolean("enabled");
            setMessage(configuration.getString("message"));
            this.trusted = new HashSet<>(configuration.getStringList("trusted"));
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        PluginManager pluginManager = plugin.getProxy().getPluginManager();
        pluginManager.registerListener(plugin, new LockdownListener(this));
        pluginManager.registerCommand(plugin, new LockdownCommand(this));
        return true;
    }

    public String getMessage() {
        return message != null ? message : "Scheduled maintenance.";
    }

    public void setMessage(String message) {
        this.message = message;
        this.centeredMotdMessage = MotdUtil.center(ChatColor.RED + "Lockdown: " + ChatColor.WHITE + getMessage());
    }

    // Using blocking IO as lockdown could help for security.
    public void save() throws IOException {
        Configuration configuration = new Configuration();
        configuration.set("enabled", enabled);
        configuration.set("message", message);
        configuration.set("trusted", new ArrayList<>(trusted));
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, lockdownFile);
    }

}
