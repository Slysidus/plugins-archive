package net.lightning.proxy.modules.motd;

import lombok.Getter;
import lombok.Setter;
import net.lightning.api.util.FileUtil;
import net.lightning.proxy.LightningProxy;
import net.lightning.proxy.modules.LightningProxyModule;
import net.lightning.proxy.util.MotdUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Getter
public class MotdModule extends LightningProxyModule implements Listener {

    private final String name = "Motd";

    private final File motdFile;

    @Setter
    private String firstLine, secondLine;
    private long faviconCache;
    private Set<String> faviconRequested;
    private Favicon nullFavicon;

    private TextComponent motdCache;

    public MotdModule(LightningProxy plugin) {
        super(plugin);
        this.motdFile = new File(plugin.getDataFolder(), "motd.yml");
    }

    @Override
    public boolean register() {
        try {
            FileUtil.checkFilePersistence(motdFile);
            Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(motdFile);
            this.firstLine = ChatColor.translateAlternateColorCodes('&', configuration.getString("first-line", ""));
            this.secondLine = ChatColor.translateAlternateColorCodes('&', configuration.getString("second-line"));
            this.faviconCache = configuration.getLong("favicon-cache", 30000);
            this.faviconRequested = faviconCache > 1 ? new HashSet<>() : null;
            //noinspection deprecation
            this.nullFavicon = faviconCache > 1 ? Favicon.create("") : null;

            reloadMotdCache();
        }
        catch (IOException exception) {
            exception.printStackTrace();
            return false;
        }

        PluginManager pluginManager = plugin.getProxy().getPluginManager();
        pluginManager.registerListener(plugin, this);
        if (faviconRequested != null) {
            plugin.getProxy().getScheduler().schedule(plugin, faviconRequested::clear, faviconCache, TimeUnit.MILLISECONDS);
        }
        return true;
    }

    public void reloadMotdCache() {
        this.motdCache = new TextComponent(MotdUtil.center(firstLine) + "\n" + MotdUtil.center(secondLine));
    }

    @EventHandler
    public void onProxyPing(ProxyPingEvent event) {
        if (event.getResponse() == null) {
            return;
        }

        ServerPing response = event.getResponse();
        response.setDescriptionComponent(motdCache);

        if (faviconRequested != null) {
            if (event.getConnection().getSocketAddress() instanceof InetSocketAddress) {
                InetSocketAddress socketAddress = (InetSocketAddress) event.getConnection().getSocketAddress();
                String hostAddress = socketAddress.getAddress().getHostAddress();
                if (faviconRequested.contains(hostAddress)) {
                    response.setFavicon(nullFavicon);
                }
                else {
                    faviconRequested.add(hostAddress);
                }
            }
        }
    }

}
