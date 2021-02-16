package net.lightning.core.listeners;

import lombok.AllArgsConstructor;
import net.lightning.common.models.NetworkPlayer;
import net.lightning.core.Game;
import net.lightning.core.GamePlayer;
import net.lightning.core.LightningGamePlugin;
import net.lightning.core.event.player.PlayerJoinGameEvent;
import net.lightning.core.event.player.PlayerSpawnGameEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

@AllArgsConstructor
public class GameJoinAndQuitListener implements Listener {

    private final LightningGamePlugin<?> plugin;

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (!plugin.getGame().canPlayerJoin(event.getPlayer())) {
            event.setResult(PlayerLoginEvent.Result.KICK_FULL);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);

        final Player nativePlayer = event.getPlayer();
        final UUID uniqueId = event.getPlayer().getUniqueId();
        plugin.getGame().getSyncingPlayers().add(uniqueId);

        nativePlayer.setGameMode(GameMode.SPECTATOR);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            NetworkPlayer networkPlayer = null;

            int tries = 0;
            while (networkPlayer == null && tries++ < 5) {
                networkPlayer = plugin.getCommonCacheManager().getPlayerCache().get(uniqueId);
            }

            NetworkPlayer finalNetworkPlayer = networkPlayer;
            Bukkit.getScheduler().runTask(plugin, () -> {
                if ((finalNetworkPlayer == null || !finalNetworkPlayer.isOnline()) && nativePlayer.isOnline()) {
                    nativePlayer.kickPlayer(ChatColor.DARK_RED + "Database sync error!" + ChatColor.WHITE + " (P-11)\n" +
                            ChatColor.RED + "You are not synced with the proxy!");
                    return;
                }

                Game game = plugin.getGame();

                game.getSyncingPlayers().remove(uniqueId);
                GamePlayer player = game.prepareJoin(nativePlayer, finalNetworkPlayer);
                if (player == null) {
                    event.getPlayer().kickPlayer(ChatColor.DARK_RED + "An error occurred while joining the game - Error code TYRK4.");
                    return;
                }

                String kickMessage = game.join(player);
                if (kickMessage != null) {
                    event.getPlayer().kickPlayer(kickMessage);
                }
                else {
                    plugin.getCraftServerHandler().getPacketReaderInjector().inject(nativePlayer);

                    PlayerJoinGameEvent joinEvent = new PlayerJoinGameEvent(game, player);
                    game.getEventManager().fireEvent(joinEvent);
                    if (joinEvent.isCallSpawnEvent()) {
                        game.getEventManager().fireEvent(new PlayerSpawnGameEvent(game, player));
                    }
                }
            });
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        Game game = plugin.getGame();
        Player nativePlayer = event.getPlayer();

        game.getSyncingPlayers().remove(nativePlayer.getUniqueId());
        if (!game.getPlayers().containsKey(nativePlayer.getUniqueId())) {
            return;
        }

        game.quit(game.getPlayer(nativePlayer));
        plugin.getCraftServerHandler().getPacketReaderInjector().uninject(nativePlayer);
    }

}
