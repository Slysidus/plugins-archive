package net.lightning.proxy.modules.lockdown;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.io.IOException;
import java.util.Arrays;

public class LockdownCommand extends Command {

    private final LockdownModule lockdownModule;

    public LockdownCommand(LockdownModule lockdownModule) {
        super("lockdown", "lightning.lockdown");
        this.lockdownModule = lockdownModule;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            switch (args.length > 0 ? args[0].toLowerCase() : "") {
                case "status":
                case "state":
                    sender.sendMessage(new TextComponent(ChatColor.GRAY + "Lockdown status: " +
                            (lockdownModule.isEnabled() ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled") + ChatColor.GRAY + "."));
                    break;

                case "toggle":
                    if (args.length < 2) {
                        sender.sendMessage(new TextComponent(ChatColor.RED + "For security measures, please specify which state to toggle the lockdown to."));
                        break;
                    }

                    boolean state;
                    switch (args[1].toLowerCase()) {
                        case "enable":
                        case "on":
                        case "true":
                        case "yes":
                            state = true;
                            break;

                        case "disable":
                        case "off":
                        case "false":
                        case "no":
                            state = false;
                            break;

                        default:
                            sender.sendMessage(new TextComponent(ChatColor.RED + "Usage: /lockdown toggle on/off"));
                            return;
                    }

                    if (lockdownModule.isEnabled() != state) {
                        lockdownModule.setEnabled(state);
                        lockdownModule.save();
                        sender.sendMessage(new TextComponent(ChatColor.LIGHT_PURPLE + "Server lockdown has been toggled " +
                                (lockdownModule.isEnabled() ? ChatColor.GREEN + "on" : ChatColor.RED + "off") + ChatColor.GRAY + "."));
                    }
                    else {
                        execute(sender, new String[]{"status"});
                    }
                    break;

                case "message":
                case "set":
                    if (args.length < 2) {
                        sender.sendMessage(new TextComponent(ChatColor.GRAY + "Lockdown message: " + ChatColor.WHITE + lockdownModule.getMessage()));
                    }
                    else {
                        final String message = ChatColor.translateAlternateColorCodes('&',
                                String.join(" ", Arrays.copyOfRange(args, 1, args.length)));

                        lockdownModule.setMessage(message);
                        lockdownModule.save();
                        sender.sendMessage(new TextComponent(ChatColor.LIGHT_PURPLE + "Lockdown message has been set to " + ChatColor.WHITE + message));
                    }
                    break;

                case "whitelist":
                case "whitelisted":
                case "trust":
                case "trusted":
                    if (args.length < 2) {
                        if (lockdownModule.getTrusted().isEmpty()) {
                            sender.sendMessage(new TextComponent(ChatColor.GRAY + "The lockdown whitelist is empty."));
                        }
                        else {
                            sender.sendMessage(new TextComponent(ChatColor.GRAY + "Whitelisted names: " +
                                    ChatColor.WHITE + String.join(ChatColor.GRAY + ", " + ChatColor.WHITE, lockdownModule.getTrusted())));
                        }
                    }
                    else {
                        String username = args[1];
                        if (lockdownModule.getTrusted().remove(username)) {
                            lockdownModule.save();
                            sender.sendMessage(new TextComponent(ChatColor.LIGHT_PURPLE + "User " + ChatColor.DARK_PURPLE + username + ChatColor.LIGHT_PURPLE + " has been " +
                                    "removed from the lockdown whitelist."));
                        }
                        else {
                            lockdownModule.getTrusted().add(username);
                            lockdownModule.save();
                            sender.sendMessage(new TextComponent(ChatColor.LIGHT_PURPLE + "User " + ChatColor.DARK_PURPLE + username + ChatColor.LIGHT_PURPLE + " has been " +
                                    "added to the lockdown whitelist." + ChatColor.GRAY + " (case sensitive)"));
                        }
                    }
                    break;

                default:
                    sender.sendMessage(new TextComponent(ChatColor.RED + "Usage: /lockdown status/toggle/message/whitelist"));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage(new TextComponent(ChatColor.DARK_RED + "An error occurred while attempting to save the lockdown state."));
        }
    }

}
