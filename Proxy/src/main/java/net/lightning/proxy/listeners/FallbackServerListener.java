package net.lightning.proxy.listeners;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class FallbackServerListener implements Listener {

    @EventHandler
    public void onServerKick(ServerKickEvent event) {
        if (!event.getKickedFrom().getName().startsWith("lobby")) {
            event.setCancelled(true);
            event.setCancelServer(ProxyServer.getInstance().getServerInfo("lobby"));
        }
    }

}
