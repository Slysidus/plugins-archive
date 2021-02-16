package net.lightning.core.graphics;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class ChestGUI implements ContainerGUI {

    protected final Inventory inventory;

    protected final List<GUIClickListener> clickListeners;

    public ChestGUI(Inventory inventory) {
        this.inventory = inventory;
        this.clickListeners = new ArrayList<>();
    }

    public ChestGUI(String title, int rows) {
        this.inventory = Bukkit.createInventory(this, rows * 9, title);
        this.clickListeners = new ArrayList<>();
    }

}
