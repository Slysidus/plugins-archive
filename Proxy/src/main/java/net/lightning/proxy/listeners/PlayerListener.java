package net.lightning.proxy.listeners;

import net.lightning.common.Rank;
import net.lightning.common.models.NetworkPlayer;
import net.lightning.proxy.LightningProxy;
import net.lightning.proxy.events.PlayerJoinEvent;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.Timestamp;
import java.time.Instant;

public class PlayerListener implements Listener {

    private final LightningProxy plugin;

    public PlayerListener(LightningProxy plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        final ProxiedPlayer proxiedPlayer = event.getPlayer();
        final Timestamp now = Timestamp.from(Instant.now());
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            try {
                boolean insert = false;
                NetworkPlayer player = plugin.getPlayer(proxiedPlayer.getUniqueId());
                if (player == null) {
                    player = new NetworkPlayer(
                            proxiedPlayer.getUniqueId(),
                            proxiedPlayer.getName(), // TODO : HANDLE NAME CHANGE CONFLICTS
                            Rank.PLAYER,
                            true,
                            now,
                            now,
                            0,
                            0,
                            0
                    );
                    insert = true;
                }

                if (!insert) {
                    player.setOnline(true);
                    player.setLastLogin(now);
                }
                plugin.getCommonCacheManager().getPlayerCache().put(player);
                plugin.getProxy().getPluginManager().callEvent(new PlayerJoinEvent(proxiedPlayer, player));

                if (insert) {
                    try {
                        plugin.getPlayerTable().insert(player);
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                        proxiedPlayer.disconnect(new TextComponent(ChatColor.DARK_RED + "Database error occurred! " + ChatColor.WHITE + "(P-02)\n" +
                                ChatColor.RED + "Unable to register you!"));
                    }
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
                proxiedPlayer.disconnect(new TextComponent(ChatColor.DARK_RED + "Database error occurred! " + ChatColor.WHITE + "(P-01)\n" +
                        ChatColor.RED + "You are not synced with the proxy!"));
            }
        });

        proxiedPlayer.setTabHeader(
                new TextComponent("\n" + ChatColor.LIGHT_PURPLE + "Lumaze Network" + "\n" + ChatColor.GRAY + "Currently in BETA.\n"),
                new TextComponent(ChatColor.LIGHT_PURPLE + "play.lumaze.net"));
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            NetworkPlayer player = plugin.getCommonCacheManager().getPlayerCache().get(event.getPlayer().getUniqueId());
            player.setOnline(false);
            plugin.getCommonCacheManager().getPlayerCache().put(player);

            try {
                plugin.getPlayerTable().update(player, "unique_id", player.getUniqueId());
            }
            catch (Exception ex) {
                ex.printStackTrace();
                plugin.getLogger().severe("Unable to save player state in database!! (" + event.getPlayer().getName() + ")");
            }
        });
    }

}
