package net.lightning.core.server;

import org.bukkit.plugin.java.JavaPlugin;

public final class FakeJavaPlugin extends JavaPlugin {

    @Override
    public void onDisable() {
        throw new IllegalStateException();
    }

    @Override
    public void onEnable() {
        throw new IllegalStateException();
    }

}
