package net.lightning.core.graphics;

import net.lightning.core.GamePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface GUIClickListener {

    /**
     * Called when the GUI is clicked. Ignores empty slots.
     *
     * @param inventory the bukkit inventory clicked
     * @param player    the player who clicks
     * @param itemStack the clicked item stack
     * @param slot      the clicked slot
     */
    void onClick(Inventory inventory, GamePlayer player, ItemStack itemStack, int slot);

}
