package net.lightning.lobby.commands;

import net.lightning.core.GamePlayer;
import net.lightning.core.LightningGamePlugin;
import net.lightning.core.commands.PlayerCoreCommand;
import net.lightning.lobby.LobbyMapModel;
import org.bukkit.ChatColor;

public class SpawnCommand extends PlayerCoreCommand {

    public SpawnCommand(LightningGamePlugin<?> plugin) {
        super(plugin, "spawn");
    }

    @Override
    public void execute(GamePlayer player, String[] args) {
        player.naiveTeleport(((LobbyMapModel) plugin.getGame().getMap()).spawn);
        player.sendUnlocalizedMessage(ChatColor.GREEN + "Home sweet home, you've been teleported back to the spawn.");
    }

}
