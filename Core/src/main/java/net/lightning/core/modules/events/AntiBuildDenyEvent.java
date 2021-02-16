package net.lightning.core.modules.events;

import lombok.Getter;
import net.lightning.core.Game;
import net.lightning.core.GamePlayer;
import net.lightning.core.event.GameEvent;
import net.lightning.core.modules.AntiBuildModule;
import org.bukkit.block.Block;

@Getter
public class AntiBuildDenyEvent extends GameEvent {

    private final GamePlayer player;

    private final Block block;
    private final AntiBuildModule.AntiBuildDenyReason reason;

    public AntiBuildDenyEvent(Game game, GamePlayer player, Block block, AntiBuildModule.AntiBuildDenyReason reason) {
        super(game);
        this.player = player;
        this.block = block;
        this.reason = reason;
    }

}
