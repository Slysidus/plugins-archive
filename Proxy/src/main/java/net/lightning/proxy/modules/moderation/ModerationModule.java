package net.lightning.proxy.modules.moderation;

import lombok.Getter;
import net.lightning.api.database.model.ModelAccessor;
import net.lightning.api.database.model.ModelManager;
import net.lightning.common.models.Sanction;
import net.lightning.proxy.LightningProxy;
import net.lightning.proxy.ProxyUserMemoryCache;
import net.lightning.proxy.modules.LightningProxyModule;
import net.md_5.bungee.api.plugin.PluginManager;

@Getter
public class ModerationModule extends LightningProxyModule {

    private final String name = "Moderation";

    private ModelAccessor<Sanction>.AccessorContext sanctionTable;

    private final ProxyUserMemoryCache<Sanction> banCache, muteCache;

    public ModerationModule(LightningProxy plugin) {
        super(plugin);

        this.banCache = new ProxyUserMemoryCache<>();
        this.muteCache = new ProxyUserMemoryCache<>();
    }

    @Override
    public boolean register() {
        try {
            sanctionTable = ModelManager.getAccessor(Sanction.class).new AccessorContext(plugin.getDatabase());
            sanctionTable.createTable(true);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        PluginManager pluginManager = plugin.getProxy().getPluginManager();
        pluginManager.registerListener(plugin, new ModerationListener(this));
        return true;
    }

}
