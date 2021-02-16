package net.lightning.proxy.modules.friends;

import net.lightning.api.commands.CommandExecutor;
import net.lightning.proxy.commands.ProxyCommand;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;

public class FriendsCommand extends ProxyCommand {

    public FriendsCommand() {
        super(CommandExecutor.PLAYER, "friends");

        helpMap.put("list", "List your friends. Default when no argument.");
        helpMap.put("add <player>", "Send a friend request.");
        helpMap.put("remove <player>", "Remove a friend.");
    }

    @Override
    public void execute(CommandSender sender, String[] args, CommandExecutor commandExecutor) {
        switch (getArgument(args, 0)) {
            case "help":
            case "?":
                displayHelp(sender, "Friend", ChatColor.YELLOW, ChatColor.GRAY, ChatColor.WHITE);

            case "list":
            case "l":
            default:
                sendMessage(sender, ChatColor.YELLOW + "You haven't added any friend yet. Use '/friend add <player>' to send a friend request!");
        }
    }

}
