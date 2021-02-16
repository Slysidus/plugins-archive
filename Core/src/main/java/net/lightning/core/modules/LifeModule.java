package net.lightning.core.modules;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.lightning.core.Game;
import net.lightning.core.GamePlayer;
import net.lightning.core.modules.events.PlayerAboutToDieLifeEvent;
import net.lightning.core.modules.events.PlayerDiedLifeEvent;
import net.lightning.core.stats.FightGameStats;
import net.lightning.core.util.ArrayPool;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

@Getter
@Setter
@Builder
public class LifeModule extends GameModule<Game> implements Listener {

    private final String name = "Life";

    private boolean autoRespawn, keepInventory, keepExp, trackKills, fakeDeath;
    private int noDamageTicks;

    private final ArrayPool.PredicatePool<GamePlayer> invincibilityChecks;
    private final ArrayPool.BiPredicatePool<GamePlayer, ItemStack> keepItemFilters;

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player nativePlayer = (Player) event.getEntity();
            GamePlayer player = game.getPlayer(nativePlayer);

            if (invincibilityChecks.testAll(player, false)) {
                event.setCancelled(true);
                return;
            }

            if (nativePlayer.getHealth() - event.getFinalDamage() <= 0) {
                PlayerAboutToDieLifeEvent gameEvent = new PlayerAboutToDieLifeEvent(game, player, event);
                if (fakeDeath) {
                    gameEvent.setResult(PlayerAboutToDieLifeEvent.Result.FAKE_DEATH);
                }
                game.getEventManager().fireEvent(gameEvent);

                PlayerAboutToDieLifeEvent.Result result = gameEvent.getResult();
                if (result == PlayerAboutToDieLifeEvent.Result.CANCEL_FATAL_HIT) {
                    event.setCancelled(true);
                }
                else if (result == PlayerAboutToDieLifeEvent.Result.FAKE_DEATH) {
                    event.setCancelled(true);
                    if (event.getCause() != EntityDamageEvent.DamageCause.CUSTOM) {
                        actualDeath(player, event.getCause());
                    }

                    PlayerDiedLifeEvent dieEvent = new PlayerDiedLifeEvent(game, player, event, keepInventory, keepExp);
                    game.getEventManager().fireEvent(dieEvent);

                    if (!keepItemFilters.isEmpty()) {
                        handlePlayerInventoryOnDeath(player, dieEvent);
                    }
                    else if (!dieEvent.isKeepInventory()) {
                        PlayerInventory playerInventory = nativePlayer.getInventory();
                        playerInventory.clear();
                        playerInventory.setArmorContents(new ItemStack[4]);
                    }

                    if (!dieEvent.isKeepExp()) {
                        nativePlayer.setExp(0);
                        nativePlayer.setTotalExperience(0);
                    }

                    if (nativePlayer.getOpenInventory() != null) {
                        nativePlayer.closeInventory();
                    }

                    nativePlayer.setNoDamageTicks(noDamageTicks);
                    nativePlayer.setFoodLevel(20);
                    nativePlayer.setSaturation(0);
                    nativePlayer.setHealth(nativePlayer.getMaxHealth());
                    for (PotionEffect activePotionEffect : nativePlayer.getActivePotionEffects()) {
                        nativePlayer.removePotionEffect(activePotionEffect.getType());
                    }
                    nativePlayer.setFlySpeed(0.1f);
                    nativePlayer.setWalkSpeed(0.2f);

                    player.clearTitle();
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player nativePlayer = event.getEntity();
        GamePlayer player = game.getPlayer(nativePlayer);

        event.setDeathMessage(null);
        actualDeath(player, nativePlayer.getLastDamageCause().getCause());

        PlayerDiedLifeEvent gameEvent = new PlayerDiedLifeEvent(game, player, event, keepInventory, keepExp);
        game.getEventManager().fireEvent(gameEvent);

        if (!keepItemFilters.isEmpty()) {
            event.setKeepInventory(true);
            handlePlayerInventoryOnDeath(player, gameEvent);
        }
        else {
            event.setKeepInventory(gameEvent.isKeepInventory());
        }
        event.setKeepLevel(gameEvent.isKeepExp());

        if (autoRespawn) {
            Bukkit.getScheduler().runTaskLater(game.getPlugin(), () -> nativePlayer.spigot().respawn(), 1);
        }
    }

    private void actualDeath(GamePlayer player, EntityDamageEvent.DamageCause cause) {
        if (player.getGameStats() instanceof FightGameStats) {
            FightGameStats playerStats = (FightGameStats) player.getGameStats();
            playerStats.setDeaths((short) (playerStats.getDeaths() + 1));
        }

        Player nativePlayer = player.getNativePlayer();
        if (nativePlayer.getKiller() != null && game.getPlayers().containsKey(nativePlayer.getKiller().getUniqueId())) {
            Player nativeKiller = nativePlayer.getKiller();
            GamePlayer killer = game.getPlayer(nativeKiller);

            game.broadcast(player.getTeam().getColor() + player.getName() + ChatColor.GRAY + " was slain by " +
                    killer.getTeam().getColor() + killer.getName() + ChatColor.GRAY + ".");
            nativeKiller.playSound(nativeKiller.getLocation(), Sound.SUCCESSFUL_HIT, 1f, 0);
        }
        else if (cause == EntityDamageEvent.DamageCause.VOID) {
            game.broadcast(player.getTeam().getColor() + player.getName() + ChatColor.GRAY + " fell into the void.");
        }
        else {
            game.broadcast(player.getTeam().getColor() + player.getName() + ChatColor.GRAY + " died.");
        }
    }

    private void handlePlayerInventoryOnDeath(GamePlayer player, PlayerDiedLifeEvent gameEvent) {
        Player nativePlayer = player.getNativePlayer();
        PlayerInventory playerInventory = nativePlayer.getInventory();
        for (int i = 0; i < 40; i++) {
            ItemStack itemStack = playerInventory.getItem(i);
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }

            if (!keepItemFilters.testAll(player, itemStack)) {
                playerInventory.setItem(i, null);
                if (!gameEvent.isKeepInventory()) {
                    nativePlayer.getWorld().dropItemNaturally(nativePlayer.getLocation(), itemStack);
                }
            }
        }
    }

    @Getter
    @AllArgsConstructor
    public static class PlayerDeath {

        private final GamePlayer player;

    }

    public static class LifeModuleBuilder {

        LifeModuleBuilder() {
            this.autoRespawn = true;
            this.noDamageTicks = 2 * 20;

            this.invincibilityChecks = new ArrayPool.PredicatePool<>();
            this.keepItemFilters = new ArrayPool.BiPredicatePool<>();
        }

        public LifeModuleBuilder invincibilityCheck(Predicate<GamePlayer> invincibilityCheck) {
            invincibilityChecks.add(invincibilityCheck);
            return this;
        }

        public LifeModuleBuilder makeInvincible() {
            invincibilityChecks.clear();
            invincibilityChecks.add(player -> true);
            return this;
        }

        public LifeModuleBuilder keepItemFilter(BiPredicate<GamePlayer, ItemStack> itemFilter) {
            keepItemFilters.add(itemFilter);
            return this;
        }

        public LifeModuleBuilder noKeepNoDrop() {
            keepInventory = true;
            return keepItemFilter((player, item) -> false);
        }

    }

}
