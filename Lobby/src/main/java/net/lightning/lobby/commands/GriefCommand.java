package net.lightning.lobby.commands;

import net.lightning.common.Rank;
import net.lightning.core.GamePlayer;
import net.lightning.core.LightningGamePlugin;
import net.lightning.core.commands.PlayerCoreCommand;
import net.lightning.lobby.LobbyPlayer;
import org.bukkit.ChatColor;

public class GriefCommand extends PlayerCoreCommand {

    public GriefCommand(LightningGamePlugin<?> plugin) {
        super(plugin, "grief", Rank.ADMIN);
    }

    @Override
    public void execute(GamePlayer player, String[] args) {
        LobbyPlayer lobbyPlayer = (LobbyPlayer) player;
        lobbyPlayer.setGriefer(!lobbyPlayer.isGriefer());
        lobbyPlayer.sendUnlocalizedMessage(ChatColor.GRAY + "Griefer mode has been toggled " +
                (lobbyPlayer.isGriefer() ? ChatColor.GREEN + "on" : ChatColor.RED + "off") + ".");
    }

}
