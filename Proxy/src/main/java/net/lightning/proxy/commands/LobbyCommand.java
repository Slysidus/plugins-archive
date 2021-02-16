package net.lightning.proxy.commands;

import net.lightning.api.commands.CommandExecutor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class LobbyCommand extends ProxyCommand {

    public LobbyCommand() {
        super(CommandExecutor.PLAYER, "lobby", null, "hub");
    }

    @Override
    public void execute(CommandSender sender, String[] args, CommandExecutor commandExecutor) {
        ProxiedPlayer player = (ProxiedPlayer) sender;
        if (player.getServer().getInfo().getName().startsWith("lobby")) {
            sendMessage(player, ChatColor.RED + "You are already in a lobby.");
        }
        else {
            player.connect(ProxyServer.getInstance().getServerInfo("lobby"));
            sendMessage(player, ChatColor.GREEN + "You've been teleported to the lobby.");
        }
    }

}
