package net.lightning.proxy.modules.moderation;

import net.lightning.api.database.query.SelectionQuery;
import net.lightning.common.SanctionType;
import net.lightning.common.models.Sanction;
import net.lightning.proxy.ProxyUserMemoryCache;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;

public class ModerationListener implements Listener {

    private final ModerationModule moderationModule;

    private final ProxyUserMemoryCache<Sanction> banCache, muteCache;

    public ModerationListener(ModerationModule moderationModule) {
        this.moderationModule = moderationModule;

        this.banCache = moderationModule.getBanCache();
        this.muteCache = moderationModule.getMuteCache();
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        UUID user = event.getConnection().getUniqueId();
        if (banCache.isExcluded(user)) {
            return;
        }

        Sanction sanction = banCache.get(user);
        if (sanction == null) {
            try {
                sanction = moderationModule.getSanctionTable().get(new SelectionQuery()
                        .where("type", SanctionType.BAN)
                        .where("player", user)
                        .where("active", true));
            }
            catch (Exception ex) {
                ex.printStackTrace();

                event.setCancelled(true);
                event.setCancelReason(new TextComponent(ChatColor.DARK_RED + "Database error occurred! " + ChatColor.WHITE + "(P-31)\n" +
                        ChatColor.RED + "Could not check if you are banned!"));
                return;
            }
        }

        if (sanction == null) {
            banCache.exclude(user);
            return;
        }

        if (!banCache.containsKey(user)) {
            banCache.put(user, sanction);
        }
        event.setCancelled(true);
        event.setCancelReason(new TextComponent(ChatColor.RED + "You are banned!\n" +
                ChatColor.WHITE + "Reason: " + ChatColor.LIGHT_PURPLE + sanction.getReason()));
    }

}
