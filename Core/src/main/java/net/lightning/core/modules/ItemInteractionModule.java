package net.lightning.core.modules;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.lightning.core.Game;
import net.lightning.core.GamePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;

/**
 * Enables item interactions (clicking on item in inventory and/or while holding it).
 * It's not a protection module. {@see {@link InventoryProtectionModule}} instead.
 */

@Getter
@Setter
public class ItemInteractionModule extends GameModule<Game> implements Listener {

    private final String name = "ItemInteraction";

    private final Multimap<Integer, BiConsumer<GamePlayer, ItemStack>> slotsActions;
    private boolean enableLeftClick, enableRightClick;

    @Builder
    public ItemInteractionModule(Multimap<Integer, BiConsumer<GamePlayer, ItemStack>> slotsActions,
                                 boolean enableLeftClick,
                                 boolean enableRightClick) {
        this.slotsActions = slotsActions;
        this.enableLeftClick = enableLeftClick;
        this.enableRightClick = enableRightClick;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player nativePlayer = (Player) event.getWhoClicked();
        if (nativePlayer.getInventory().equals(event.getClickedInventory())) {
            if (!slotsActions.containsKey(event.getSlot())) {
                return;
            }

            GamePlayer player = game.getPlayer(nativePlayer);
            for (BiConsumer<GamePlayer, ItemStack> callback : slotsActions.get(event.getSlot())) {
                callback.accept(player, event.getCurrentItem());
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if ((enableLeftClick && (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK))
                || (enableRightClick && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK))) {
            Player nativePlayer = event.getPlayer();
            int slot = nativePlayer.getInventory().getHeldItemSlot();
            if (!slotsActions.containsKey(slot)
                    || !nativePlayer.getInventory().getItem(slot).isSimilar(event.getItem())) {
                return;
            }

            GamePlayer player = game.getPlayer(nativePlayer);
            for (BiConsumer<GamePlayer, ItemStack> callback : slotsActions.get(slot)) {
                callback.accept(player, event.getItem());
            }
        }
    }

    public static class ItemInteractionModuleBuilder {

        ItemInteractionModuleBuilder() {
            this.slotsActions = HashMultimap.create();

            this.enableLeftClick = true;
            this.enableRightClick = true;
        }

        public ItemInteractionModuleBuilder slotAction(int slot, BiConsumer<GamePlayer, ItemStack> action) {
            slotsActions.put(slot, action);
            return this;
        }

    }

}
