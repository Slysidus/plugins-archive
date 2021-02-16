package net.lightning.core.modules;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import net.lightning.core.Game;
import net.lightning.core.GamePlayer;
import net.lightning.core.util.ArrayPool;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;

@Getter
public class InventoryProtectionModule extends GameModule<Game> implements Listener {

    private final String name = "InventoryProtection";

    private final Set<InventoryType.SlotType> deniedSlots;
    private final ArrayPool.BiPredicatePool<GamePlayer, Item> dropChecks;

    @Builder
    public InventoryProtectionModule(@Singular Set<InventoryType.SlotType> deniedSlots,
                                     @Singular List<BiPredicate<GamePlayer, Item>> dropChecks) {
        this.deniedSlots = deniedSlots;
        this.dropChecks = new ArrayPool.BiPredicatePool<>(dropChecks);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (deniedSlots.contains(event.getSlotType())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        GamePlayer gamePlayer = game.getPlayer(event.getPlayer());
        if (!dropChecks.testAll(gamePlayer, event.getItemDrop())) {
            event.setCancelled(true);
        }
    }

    public static class InventoryProtectionModuleBuilder {

        public InventoryProtectionModuleBuilder denyAll() {
            deniedSlots(Arrays.asList(InventoryType.SlotType.values()));
            return this;
        }

    }

}
