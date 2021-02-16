package net.lightning.proxy.modules.moderation.commands;

import net.lightning.api.commands.CommandExecutor;
import net.lightning.common.models.NetworkPlayer;
import net.lightning.proxy.commands.ProxyCommand;
import net.md_5.bungee.api.CommandSender;

public class BanCommand extends ProxyCommand {

    public BanCommand() {
        super("ban", null, "tempban", "gban");
    }

    @Override
    public void execute(CommandSender sender, String[] args, CommandExecutor commandExecutor) {
        String playerName = getArgument(args, 0);
        if (args.length == 1) {
            NetworkPlayer networkPlayer = getPlayer(playerName);
    }

}
