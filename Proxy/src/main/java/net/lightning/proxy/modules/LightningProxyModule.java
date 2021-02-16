package net.lightning.proxy.modules;

import lombok.Getter;
import net.lightning.proxy.LightningProxy;

public abstract class LightningProxyModule {

    @Getter
    protected final LightningProxy plugin;

    protected LightningProxyModule(LightningProxy plugin) {
        this.plugin = plugin;
    }

    public abstract String getName();

    public abstract boolean register();

}
