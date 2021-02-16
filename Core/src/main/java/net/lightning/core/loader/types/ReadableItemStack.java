package net.lightning.core.loader.types;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ReadableItemStack extends ItemStack {

    public ReadableItemStack() {
    }

    public ReadableItemStack(Material type) {
        super(type);
    }

    public ReadableItemStack(Material type, int amount) {
        super(type, amount);
    }

    public ReadableItemStack(Material type, int amount, short damage) {
        super(type, amount, damage);
    }

    public ReadableItemStack(ItemStack stack) throws IllegalArgumentException {
        super(stack);
    }

}
