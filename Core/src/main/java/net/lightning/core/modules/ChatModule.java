package net.lightning.core.modules;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.lightning.common.Rank;
import net.lightning.core.Game;
import net.lightning.core.GamePlayer;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.function.Function;

@Getter
@Setter
public class ChatModule extends GameModule<Game> implements Listener {

    private final String name = "Chat";

    private Rank allowColors;

    private final Function<GamePlayer, String> formatter;

    @Builder
    public ChatModule(Function<GamePlayer, String> formatter, Rank allowColors) {
        this.formatter = formatter != null ? formatter : this::defaultFormatter;
        this.allowColors = allowColors;
    }

    @EventHandler(ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if (game.getSyncingPlayers().contains(event.getPlayer().getUniqueId())) {
            event.getPlayer().sendMessage(ChatColor.RED + "You are still syncing with the network! This should only take a few millis.");
            event.setCancelled(true);
            return;
        }

        GamePlayer player = game.getPlayer(event.getPlayer());
        if (allowColors != null && player.getNetworkPlayer().getRank().isAboveOrEquals(allowColors)) {
            event.setMessage(ChatColor.translateAlternateColorCodes('&', event.getMessage()));
        }

        final String format = formatter.apply(player);
        if (format != null) {
            event.setFormat(format);
        }
        else {
            event.setCancelled(true);
        }
    }

    private String defaultFormatter(GamePlayer player) {
        if (player.getTeam() != null) {
            return player.getTeam().getColor() + "%s" + ChatColor.DARK_GRAY + " ⤜ " + ChatColor.WHITE + "%s";
        }
        return player.getNetworkPlayer().getRank().getColor() + "%s" + ChatColor.DARK_GRAY + " ⤜ " + ChatColor.WHITE + "%s";
    }

}
