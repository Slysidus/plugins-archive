package net.lightning.core.modules;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.lightning.core.Game;
import net.lightning.core.event.GameEventHandler;
import net.lightning.core.event.GameEventPriority;
import net.lightning.core.event.GameListener;
import net.lightning.core.event.player.PlayerJoinGameEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

@Getter
@Setter
@Builder
public class PlayerKillerModule extends GameModule<Game> implements Listener, GameListener {

    private final String name = "PlayerKiller";

    private int voidLimit;
    private Location teleportInstead;

    private boolean killOnJoin, cancelFirstSpawnEvent;

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo().getBlockY() < voidLimit) {
            if (teleportInstead != null && teleportInstead.getY() > voidLimit) {
                event.getPlayer().teleport(teleportInstead);
            }
            else {
                final double damage = event.getPlayer().getHealth() + 1f;
                EntityDamageEvent entityDamageEvent = new EntityDamageEvent(event.getPlayer(), EntityDamageEvent.DamageCause.VOID, damage);
                Bukkit.getPluginManager().callEvent(entityDamageEvent);
                if (!entityDamageEvent.isCancelled()) {
                    event.getPlayer().damage(damage);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @GameEventHandler(priority = GameEventPriority.LOW)
    public void onGamePlayerJoin(PlayerJoinGameEvent event) {
        if (!killOnJoin) {
            return;
        }

        Player nativePlayer = event.getPlayer().getNativePlayer();
        final double damage = nativePlayer.getHealth() + 1f;
        EntityDamageEvent entityDamageEvent = new EntityDamageEvent(nativePlayer, EntityDamageEvent.DamageCause.CUSTOM, damage);
        Bukkit.getPluginManager().callEvent(entityDamageEvent);
        if (!entityDamageEvent.isCancelled()) {
            nativePlayer.damage(damage);
        }

        if (cancelFirstSpawnEvent) {
            event.setCallSpawnEvent(false);
        }
    }

    public static class PlayerKillerModuleBuilder {

        PlayerKillerModuleBuilder() {
            this.killOnJoin = true;
            this.cancelFirstSpawnEvent = true;
        }

    }

}
