package net.lightning.capture.graphics;

import net.lightning.capture.game.CaptureGamePlayer;
import net.lightning.capture.game.CaptureKit;
import net.lightning.core.GamePlayer;
import net.lightning.core.graphics.ChestGUI;
import net.lightning.core.graphics.GUIClickListener;
import net.lightning.core.util.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class KitSelectorGUI extends ChestGUI implements GUIClickListener {

    public KitSelectorGUI() {
        super(ChatColor.GRAY + "Select a kit", 3);
        clickListeners.add(this);
    }

    public void update(CaptureKit selected) {
        inventory.setItem(11, new ItemBuilder(Material.IRON_SWORD)
                .setDisplayName(ChatColor.YELLOW + "Soldier")
                .setLore(
                        ChatColor.GRAY + "Includes:",
                        ChatColor.GRAY + "- " + ChatColor.YELLOW + "Iron sword & Leather armor",
                        ChatColor.GRAY + "- " + ChatColor.YELLOW + "Gold pickaxe",
                        ChatColor.GRAY + "- " + ChatColor.YELLOW + "2 Golden apples",
                        " ",
                        selected == CaptureKit.SOLDIER ? ChatColor.GREEN + "Selected." : ChatColor.DARK_GREEN + "Click to select."
                )
                .addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                .build());
        inventory.setItem(13, new ItemBuilder(Material.BOW)
                .setDisplayName(ChatColor.YELLOW + "Archer")
                .setLore(
                        ChatColor.GRAY + "Includes:",
                        ChatColor.GRAY + "- " + ChatColor.YELLOW + "Stone sword with Leather armor",
                        ChatColor.GRAY + "- " + ChatColor.YELLOW + "Bow with 32 arrows",
                        ChatColor.GRAY + "- " + ChatColor.YELLOW + "Gold pickaxe",
                        ChatColor.GRAY + "- " + ChatColor.YELLOW + "2 Golden apples",
                        " ",
                        selected == CaptureKit.ARCHER ? ChatColor.GREEN + "Selected." : ChatColor.DARK_GREEN + "Click to select."
                )
                .addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                .build());
        inventory.setItem(15, new ItemBuilder(Material.CHAINMAIL_CHESTPLATE)
                .setDisplayName(ChatColor.YELLOW + "Hedgehog")
                .setLore(
                        ChatColor.GRAY + "Includes:",
                        ChatColor.GRAY + "- " + ChatColor.YELLOW + "A fist",
                        ChatColor.GRAY + "- " + ChatColor.YELLOW + "Chainmail chestplate with thorns",
                        ChatColor.GRAY + "- " + ChatColor.YELLOW + "Wood pickaxe",
                        ChatColor.GRAY + "- " + ChatColor.YELLOW + "1 Golden apple",
                        " ",
                        ChatColor.GRAY + "Effects:",
                        ChatColor.GRAY + "- " + ChatColor.AQUA + "Speed 1",
                        " ",
                        selected == CaptureKit.HEDGEHOG ? ChatColor.GREEN + "Selected." : ChatColor.DARK_GREEN + "Click to select."
                )
                .addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                .build());
    }

    @Override
    public void onClick(Inventory inventory, GamePlayer player, ItemStack itemStack, int slot) {
        CaptureKit clickedKit;
        switch (slot) {
            case 11:
                clickedKit = CaptureKit.SOLDIER;
                break;
            case 13:
                clickedKit = CaptureKit.ARCHER;
                break;
            case 15:
                clickedKit = CaptureKit.HEDGEHOG;
                break;
            default:
                return;
        }

        CaptureGamePlayer captureGamePlayer = (CaptureGamePlayer) player;
        if (captureGamePlayer.getSelectedKit() != clickedKit) {
            captureGamePlayer.setSelectedKit(clickedKit);
            player.getNativePlayer().closeInventory();
            player.sendUnlocalizedMessage(ChatColor.GREEN + "You selected the kit " + clickedKit.getName() + ".");
        }
    }

}
