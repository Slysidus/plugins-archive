package net.lightning.core;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Most of the time, it should not be use as NPE come from developing mistakes.
 * However, it helps as I know that every game player CANNOT be null (not just shouldn't).
 * <p>
 * Game players are null after reload. No join event is faked (atm).
 */

@UtilityClass
public class GameNPECatcher {

    public void validateGameState(Game game) {
        if (game == null) {
            Bukkit.shutdown();
            return;
        }

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!game.getPlayers().containsKey(onlinePlayer.getUniqueId())
                    && !game.getSyncingPlayers().contains(onlinePlayer.getUniqueId())) {
                onlinePlayer.kickPlayer(ChatColor.DARK_RED + "Player out of sync!\nYou are not in the game.");
            }
        }
    }

}
