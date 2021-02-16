package net.lightning.core.util;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

/**
 * Create an {@link ItemStack} using the builder pattern.
 */
public class ItemBuilder {

    private final ItemStack itemStack;
    private final ItemMeta itemMeta;

    /**
     * Builds upon an existing item
     *
     * @param itemStack item
     */
    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.itemMeta = itemStack.getItemMeta();
    }

    /**
     * Creates a new item and builds upon it
     *
     * @param material item material
     */
    public ItemBuilder(Material material) {
        this(new ItemStack(material));
    }

    /**
     * Creates a new item and builds upon it
     *
     * @param material item material
     * @param amount   item amount
     */
    public ItemBuilder(Material material, int amount) {
        this(new ItemStack(material, amount));
    }

    /**
     * Creates a new skull item and builds upon it
     *
     * @param skullType skull type
     */
    public ItemBuilder(SkullType skullType) {
        this(Material.SKULL_ITEM);
        setSkullType(skullType);
    }

    /**
     * Sets the amount in the stack
     *
     * @param amount new amount
     * @return this builder
     */
    public ItemBuilder setAmount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    /**
     * Sets the durability of the item
     *
     * @param durability new durability
     * @return this builder
     */
    public ItemBuilder setDurability(short durability) {
        itemStack.setDurability(durability);
        return this;
    }

    /**
     * Sets the display name of the item
     *
     * @param displayName new display name
     * @return this builder
     */
    public ItemBuilder setDisplayName(String displayName) {
        itemMeta.setDisplayName(displayName);
        return this;
    }

    /**
     * Sets the lore of the item. Will replace it if already existing
     *
     * @param lines new lore lines
     * @return this builder
     */
    public ItemBuilder setLore(String... lines) {
        itemMeta.setLore(Arrays.asList(lines));
        return this;
    }

    /**
     * Adds item flags is they are not set already
     *
     * @param flags flags to add
     * @return this builder
     */
    public ItemBuilder addItemFlags(ItemFlag... flags) {
        itemMeta.addItemFlags(flags);
        return this;
    }

    /**
     * Sets the unbreakable tag
     *
     * @param unbreakable true if the item should be unbreakable
     * @return this builder
     */
    public ItemBuilder setUnbreakable(boolean unbreakable) {
        itemMeta.spigot().setUnbreakable(unbreakable);
        return this;
    }

    /**
     * Makes the item unbreakable and hides it
     *
     * @return this builder
     */
    public ItemBuilder makeUnbreakable() {
        setUnbreakable(true);
        addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        return this;
    }

    public ItemBuilder addEnchantment(Enchantment enchantment, int level) {
        itemMeta.addEnchant(enchantment, level, true);
        return this;
    }

    /*
    Material-specific methods
     */

    /**
     * Sets the skull type if the item is a SKULL_ITEM
     *
     * @param skullType new skull type
     * @return this builder
     */
    public ItemBuilder setSkullType(SkullType skullType) {
        if (itemStack.getType() == Material.SKULL_ITEM) {
            itemStack.setDurability((short) skullType.ordinal());
        }
        return this;
    }

    /**
     * Sets the skull owner if the item is a SKULL_ITEM of type PLAYER
     *
     * @param owner new skull owner
     * @return this builder
     */
    public ItemBuilder setSkullOwner(String owner) {
        if (itemMeta instanceof SkullMeta && itemStack.getDurability() == SkullType.PLAYER.ordinal()) {
            SkullMeta skullMeta = (SkullMeta) itemMeta;
            skullMeta.setOwner(owner);
        }
        return this;
    }

    /**
     * Sets the item color if the item can be colored
     *
     * @param color new color
     * @return this builder
     */
    public ItemBuilder setColor(Color color) {
        if (itemMeta instanceof LeatherArmorMeta) {
            LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemMeta;
            leatherArmorMeta.setColor(color);
        }
        return this;
    }

    /**
     * Sets the item dye color if the item can be dye colored
     *
     * @param color new color
     * @return this builder
     */
    @SuppressWarnings("deprecation")
    public ItemBuilder setColor(DyeColor color) {
        if (itemMeta instanceof LeatherArmorMeta) {
            LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemMeta;
            leatherArmorMeta.setColor(color.getColor());
        }
        else if (itemStack.getType() == Material.WOOL
                || itemStack.getType() == Material.STAINED_CLAY) {
            setDurability(color.getData());
        }
        else if (itemStack.getType() == Material.INK_SACK) {
            setDurability(color.getDyeData());
        }
        return this;
    }

    /**
     * Sets the main potion effect if the item is a potion
     *
     * @param potionEffectType new effect type
     * @return this builder
     */
    public ItemBuilder setMainPotionEffect(PotionEffectType potionEffectType) {
        if (itemMeta instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta) itemMeta;
            potionMeta.setMainEffect(potionEffectType);
        }
        return this;
    }

    /*
    Build
     */

    /**
     * Gets the item stack
     *
     * @return the item stack
     */
    public ItemStack build() {
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    /**
     * Copies the item builder
     *
     * @return a new cloned item builder based
     */
    public ItemBuilder copy() {
        return new ItemBuilder(build().clone());
    }

    /**
     * Copies the item builder and gets the item
     *
     * @return a cloned item stack
     */
    public ItemStack copyBuild() {
        return new ItemBuilder(build().clone()).build();
    }

}
