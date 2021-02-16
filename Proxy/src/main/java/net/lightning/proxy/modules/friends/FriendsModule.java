package net.lightning.proxy.modules.friends;

import lombok.Getter;
import net.lightning.proxy.LightningProxy;
import net.lightning.proxy.modules.LightningProxyModule;
import net.md_5.bungee.api.plugin.PluginManager;

@Getter
public class FriendsModule extends LightningProxyModule {

    private final String name = "Friends";

    public FriendsModule(LightningProxy plugin) {
        super(plugin);
    }

    @Override
    public boolean register() {
        PluginManager pluginManager = plugin.getProxy().getPluginManager();
        pluginManager.registerListener(plugin, new FriendsListener());
        return true;
    }

}
