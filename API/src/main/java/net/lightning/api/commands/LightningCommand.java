package net.lightning.api.commands;

import org.jetbrains.annotations.Contract;

public interface LightningCommand {

    CommandExecutor getAllowedExecutor();

    @Contract("false, _ -> fail")
    default void checkArgument(boolean condition, String message) {
        if (!condition) {
            throw new CommandExecutionException(CommandExecutionException.Type.FAIL, message);
        }
    }

    default String getArgument(String[] args, int index) {
        if (args.length > index) {
            return args[index];
        }
        return "";
    }

    default String getArgument(String[] args, int index, String def) {
        if (args.length > index) {
            return args[index];
        }
        return def;
    }

}
