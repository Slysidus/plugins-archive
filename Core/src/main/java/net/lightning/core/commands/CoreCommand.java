package net.lightning.core.commands;

import lombok.Getter;
import net.lightning.api.commands.CommandExecutionException;
import net.lightning.api.commands.CommandExecutor;
import net.lightning.api.commands.LightningCommand;
import net.lightning.core.Game;
import net.lightning.core.LightningGamePlugin;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public abstract class CoreCommand implements org.bukkit.command.CommandExecutor, LightningCommand {

    protected final LightningGamePlugin<?> plugin;

    private final String name;

    @Getter
    private final CommandExecutor allowedExecutor;

    protected final Map<String, String> helpMap;

    public CoreCommand(LightningGamePlugin<?> plugin, String name) {
        this(plugin, CommandExecutor.BOTH, name);
    }

    public CoreCommand(LightningGamePlugin<?> plugin, CommandExecutor allowedExecutor, String name) {
        this.plugin = plugin;
        this.allowedExecutor = allowedExecutor;
        this.name = name;

        this.helpMap = new HashMap<>();
    }

    @Override
    public final boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equals(name)) {
            throw new IllegalStateException("Command executor should only executes its assigned command.");
        }

        try {
            final boolean isPlayer = sender instanceof Player;
            if ((isPlayer && allowedExecutor == CommandExecutor.CONSOLE)
                    || (!isPlayer && allowedExecutor == CommandExecutor.PLAYER)) {
                throw new CommandExecutionException(CommandExecutionException.Type.DENIED, "You cannot use this command.");
            }

            execute(sender, args, isPlayer ? CommandExecutor.PLAYER : CommandExecutor.CONSOLE);
        }
        catch (CommandExecutionException ex) {
            onExecutionException(sender, args, ex);
        }
        return true;
    }

    public void onExecutionException(CommandSender sender, String[] args, CommandExecutionException exception) {
        sender.sendMessage(ChatColor.RED + exception.getMessage());
    }

    public abstract void execute(CommandSender sender, String[] args, CommandExecutor commandExecutor);

    public void displayHelp(CommandSender sender, String name, ChatColor mainColor, ChatColor sepColor, ChatColor descColor) {
        sender.sendMessage(mainColor.toString() + ChatColor.BOLD + name + " command help:");
        helpMap.forEach((command, desc) -> sender.sendMessage(mainColor + "/" + command + sepColor + " - " + descColor + desc));
    }

    /*
    QoL
     */

    public Game getGame() {
        return plugin.getGame();
    }

    public void registerSelf() {
        plugin.getCommand(name).setExecutor(this);
    }

}
