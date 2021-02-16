package net.lightning.core.commands;

import net.lightning.api.commands.CommandExecutionException;
import net.lightning.api.commands.CommandExecutor;
import net.lightning.common.Rank;
import net.lightning.core.GamePlayer;
import net.lightning.core.LightningGamePlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class PlayerCoreCommand extends CoreCommand {

    private final Rank requiredRank;

    public PlayerCoreCommand(LightningGamePlugin<?> plugin, String name) {
        this(plugin, name, Rank.PLAYER);
    }

    public PlayerCoreCommand(LightningGamePlugin<?> plugin, String name, Rank requiredRank) {
        super(plugin, CommandExecutor.PLAYER, name);
        this.requiredRank = requiredRank;
    }

    @Override
    public final void execute(CommandSender sender, String[] args, CommandExecutor commandExecutor) {
        GamePlayer player = getGame().getPlayer((Player) sender);
        if (!player.getNetworkPlayer().hasRankOrAbove(requiredRank)) {
            throw new CommandExecutionException(CommandExecutionException.Type.DENIED, "You are missing permissions to use this command.");
        }

        execute(player, args);
    }

    public abstract void execute(GamePlayer player, String[] args);

}
