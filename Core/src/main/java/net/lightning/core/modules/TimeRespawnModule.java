package net.lightning.core.modules;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.lightning.core.Game;
import net.lightning.core.GamePlayer;
import net.lightning.core.event.GameEventHandler;
import net.lightning.core.event.GameListener;
import net.lightning.core.modules.events.PlayerDiedLifeEvent;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Getter
@Setter
@Builder
public class TimeRespawnModule extends GameModule<Game> implements GameListener {

    private final String name = "TimeRespawn";
    private final List<Class<? extends GameModule<?>>> dependencies = Collections.singletonList(LifeModule.class);

    private Location teleportLocation;
    private int respawnAfter;

    private final ConcurrentMap<GamePlayer, TimeRespawnTask> respawning = new ConcurrentHashMap<>();

    @GameEventHandler
    public void onPlayerDiedEvent(PlayerDiedLifeEvent event) {
        GamePlayer player = event.getPlayer();

        Player nativePlayer = player.getNativePlayer();
        nativePlayer.setGameMode(GameMode.SPECTATOR);
        nativePlayer.teleport(teleportLocation);

        TimeRespawnTask task = new TimeRespawnTask(player, respawnAfter);
        task.runTaskTimer(game.getPlugin(), 0, 20);
        respawning.put(player, task);
    }

    @AllArgsConstructor
    public static class TimeRespawnTask extends BukkitRunnable {

        private final GamePlayer player;
        private int timer;

        @Override
        public void run() {
            if (player.hasDisconnected()) {
                cancel();
                return;
            }

            if (timer == 0) {
                player.clearTitle();
                player.triggerSpawnEvent();
                cancel();
            }
            else {
                player.sendTitle(ChatColor.GRAY + "You died",
                        ChatColor.WHITE + "Please wait " + ChatColor.YELLOW + timer + ChatColor.WHITE + " second" + (timer == 1 ? "." : "s."),
                        2, 20, 0);
                player.playSound(Sound.NOTE_BASS);
                timer--;
            }
        }

    }

}
