package net.lightning.proxy.commands;

import lombok.Getter;
import net.lightning.api.commands.CommandExecutionException;
import net.lightning.api.commands.CommandExecutor;
import net.lightning.api.commands.LightningCommand;
import net.lightning.common.models.NetworkPlayer;
import net.lightning.proxy.LightningProxy;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class ProxyCommand extends Command implements LightningCommand {

    @Getter
    private final CommandExecutor allowedExecutor;

    protected final Map<String, String> helpMap;

    public ProxyCommand(String name) {
        this(CommandExecutor.BOTH, name);
    }

    public ProxyCommand(CommandExecutor allowedExecutor, String name) {
        super(name);
        this.allowedExecutor = allowedExecutor;
        this.helpMap = new HashMap<>();
    }

    public ProxyCommand(String name, String permission, String... aliases) {
        this(CommandExecutor.BOTH, name, permission, aliases);
    }

    public ProxyCommand(CommandExecutor allowedExecutor, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.allowedExecutor = allowedExecutor;
        this.helpMap = new HashMap<>();
    }

    @Override
    public final void execute(CommandSender sender, String[] args) {
        final boolean isPlayer = sender instanceof ProxiedPlayer;
        if ((isPlayer && allowedExecutor == CommandExecutor.CONSOLE)
                || (!isPlayer && allowedExecutor == CommandExecutor.PLAYER)) {
            throw new CommandExecutionException(CommandExecutionException.Type.DENIED, "You cannot use this command.");
        }

        try {
            execute(sender, args, isPlayer ? CommandExecutor.PLAYER : CommandExecutor.CONSOLE);
        }
        catch (CommandExecutionException ex) {
            onExecutionException(sender, args, ex);
        }
    }

    public void onExecutionException(CommandSender sender, String[] args, CommandExecutionException exception) {
        sendMessage(sender, ChatColor.RED + exception.getMessage());
    }

    public abstract void execute(CommandSender sender, String[] args, CommandExecutor commandExecutor);

    public void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(new TextComponent(message));
    }

    public void displayHelp(CommandSender sender, String name, ChatColor mainColor, ChatColor sepColor, ChatColor descColor) {
        sendMessage(sender, mainColor.toString() + ChatColor.BOLD + name + " command help:");
        helpMap.forEach((command, desc) -> {
            sendMessage(sender, mainColor + "/" + command + sepColor + " - " + descColor + desc);
        });
    }

    protected NetworkPlayer getPlayer(String playerName) {
        UUID targetUUID = null;
        ProxiedPlayer targetProxiedPlayer = plugin.getProxy().getPlayer(playerName);
        if (targetProxiedPlayer != null) {
            targetUUID = targetProxiedPlayer.getUniqueId();
        }
        try {
            targetUUID = UUID.fromString(playerName);
        }
        catch (IllegalArgumentException ignored) {
        }

        NetworkPlayer networkPlayer = null;
        try {
            if (targetUUID != null) {
                networkPlayer = plugin.getPlayer(targetUUID);
            }
            else {
                networkPlayer = plugin.getPlayerTable().get("username", playerName);
                if (networkPlayer == null) {
                    throw new CommandExecutionException(CommandExecutionException.Type.FAIL, "Target not found. Either the player is not online or this is an invalid uuid.");
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        checkArgument(networkPlayer != null, "This player never logged on the network.");
        return networkPlayer;
    }

}
