package net.lightning.proxy.modules.lockdown;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class LockdownListener implements Listener {

    private final LockdownModule lockdownModule;

    protected LockdownListener(LockdownModule lockdownModule) {
        this.lockdownModule = lockdownModule;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(PreLoginEvent event) {
        if (lockdownModule.isEnabled() && !lockdownModule.getTrusted().contains(event.getConnection().getName())) {
            event.setCancelled(true);
            event.setCancelReason(new TextComponent(ChatColor.RED + "The server is on lockdown.\n" +
                    ChatColor.LIGHT_PURPLE + "Reason: " + ChatColor.WHITE + lockdownModule.getMessage()));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onProxyPing(ProxyPingEvent event) {
        if (lockdownModule.isEnabled()) {
            final String newMotd = event.getResponse().getDescriptionComponent().toPlainText().split("\n")[0] + "\n" + lockdownModule.getCenteredMotdMessage();
            event.getResponse().setDescriptionComponent(new TextComponent(newMotd));
        }
    }

}
