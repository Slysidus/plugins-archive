package net.lightning.core.listeners;

import lombok.AllArgsConstructor;
import net.lightning.core.Game;
import net.lightning.core.LightningGamePlugin;
import net.lightning.core.event.player.PlayerSpawnGameEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

@AllArgsConstructor
public class NativePlayerListener implements Listener {

    private final LightningGamePlugin<?> plugin;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawnMonitor(PlayerRespawnEvent event) {
        Game game = plugin.getGame();
        if (game.getSyncingPlayers().contains(event.getPlayer().getUniqueId())) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(game.getPlugin(), () ->
                game.getEventManager().fireEvent(new PlayerSpawnGameEvent(game, game.getPlayer(event.getPlayer()))), 1);
    }

}
