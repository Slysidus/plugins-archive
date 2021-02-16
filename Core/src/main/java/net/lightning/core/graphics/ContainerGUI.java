package net.lightning.core.graphics;

import org.bukkit.inventory.InventoryHolder;

import java.util.List;

public interface ContainerGUI extends InventoryHolder {

    List<GUIClickListener> getClickListeners();

    default boolean lockInventory() {
        return true;
    }

}
