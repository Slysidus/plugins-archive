package net.lightning.lobby.graphics;

import net.lightning.core.graphics.ChestGUI;
import net.lightning.core.util.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

public class PlayMainGUI extends ChestGUI {

    public PlayMainGUI() {
        super(ChatColor.GRAY + "Choose a game", 3);
        update();
    }

    public void update() {
        inventory.setItem(13, new ItemBuilder(Material.GLOWSTONE)
                .setDisplayName(ChatColor.YELLOW + "Lightning Capture")
                .setLore(
                        ChatColor.GRAY + "Be the first team to steal all the power",
                        ChatColor.GRAY + "from the opponent's accumulator.",
                        ChatColor.GRAY + "You can join in-progress games.",
                        " ",
                        ChatColor.WHITE + "Servers: " + ChatColor.RED + "0"
                )

                .addEnchantment(Enchantment.THORNS, 1)
                .addItemFlags(ItemFlag.HIDE_ENCHANTS)
                .build());
    }

}
