package net.lightning.proxy.commands;

import net.lightning.api.commands.CommandExecutionException;
import net.lightning.api.commands.CommandExecutor;
import net.lightning.common.Rank;
import net.lightning.common.models.NetworkPlayer;
import net.lightning.proxy.LightningProxy;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class SetRankCommand extends ProxyCommand {

    private final LightningProxy plugin;

    public SetRankCommand(LightningProxy plugin) {
        super("rank", "lightning.rank");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args, CommandExecutor commandExecutor) {
        if (args.length == 0) {
            sendMessage(sender, ChatColor.RED + "Usage: /rank <player> [set <rank>]");
            return;
        }

        String playerName = getArgument(args, 0);
        if (args.length == 1) {
            NetworkPlayer networkPlayer = getPlayer(playerName);
            sendMessage(sender, ChatColor.LIGHT_PURPLE.toString() + networkPlayer.getCachedName() + " currently has the rank " +
                    networkPlayer.getRank().getColor() + networkPlayer.getRank().getDisplayName() + ChatColor.LIGHT_PURPLE + ".");
            return;
        }

        switch (getArgument(args, 1).toLowerCase()) {
            case "get":
            case "current":
                execute(sender, new String[]{playerName});
                break;

            case "set":
                NetworkPlayer networkPlayer = getPlayer(playerName);
                Rank rank = Rank.getRank(getArgument(args, 2).toUpperCase());
                checkArgument(rank != null, "The rank '" + getArgument(args, 2, "none") + "' does not exist.");
                if (sender instanceof ProxiedPlayer) {
                    checkArgument(!networkPlayer.getRank().isAboveOrEquals(Rank.DEVELOPER), "This player's rank is too high to be set by another player. The console must be used" +
                            ".");
                    checkArgument(!rank.isAboveOrEquals(Rank.DEVELOPER), "This rank cannot be set unless you are the console.");
                }

                networkPlayer.setRank(rank);
                ProxiedPlayer targetProxiedPlayer = plugin.getProxy().getPlayer(playerName);
                plugin.getProxy().getScheduler().runAsync(plugin, () -> {
                    try {
                        if (targetProxiedPlayer != null) {
                            plugin.getCommonCacheManager().getPlayerCache().put(networkPlayer);
                            plugin.sendData(targetProxiedPlayer, "RankUpdate", out -> out.writeInt(rank.ordinal()));
                        }

                        // Important thing, force update
                        plugin.getPlayerTable().update(networkPlayer, "unique_id", networkPlayer.getUniqueId());
                        sendMessage(sender, ChatColor.GREEN.toString() + networkPlayer.getCachedName() +
                                "'s rank has been set to " + rank.getColor() + rank.getDisplayName() + ChatColor.GREEN + ".");
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                        sendMessage(sender, ChatColor.DARK_RED + "Unable to set player's rank. Check to console for logs.");
                    }
                });
                break;

            default:
                sendMessage(sender, ChatColor.RED + "Usage: /rank <player> [set <rank>]");
        }
    }

}
