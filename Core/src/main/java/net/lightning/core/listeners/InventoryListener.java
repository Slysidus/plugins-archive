package net.lightning.core.listeners;

import lombok.RequiredArgsConstructor;
import net.lightning.core.Game;
import net.lightning.core.GamePlayer;
import net.lightning.core.graphics.ContainerGUI;
import net.lightning.core.graphics.GUIClickListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class InventoryListener implements Listener {

    private final Game game;

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) {
            // it should not happen but for some reasons it does
            return;
        }

        InventoryHolder inventoryHolder = event.getClickedInventory().getHolder();
        if (inventoryHolder instanceof ContainerGUI) {
            ContainerGUI containerGui = (ContainerGUI) inventoryHolder;
            if (containerGui.lockInventory()) {
                event.setCancelled(true);
            }

            Inventory inventory = event.getClickedInventory();
            GamePlayer player = game.getPlayer((Player) event.getWhoClicked());
            ItemStack itemStack = event.getCurrentItem();
            if (itemStack == null) {
                return;
            }

            for (GUIClickListener clickListener : containerGui.getClickListeners()) {
                clickListener.onClick(inventory, player, itemStack, event.getSlot());
            }
        }
        else if (event.getView().getTopInventory() != null) {
            InventoryHolder topInventoryHolder = event.getView().getTopInventory().getHolder();
            if (topInventoryHolder instanceof ContainerGUI) {
                ContainerGUI containerGUI = (ContainerGUI) topInventoryHolder;
                if (containerGUI.lockInventory()) {
                    event.setCancelled(true);
                }
            }
        }
    }

}
