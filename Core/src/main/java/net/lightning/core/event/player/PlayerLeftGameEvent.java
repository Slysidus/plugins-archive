package net.lightning.core.event.player;

import lombok.Getter;
import net.lightning.core.Game;
import net.lightning.core.GamePlayer;
import net.lightning.core.event.GameEvent;

@Getter
public class PlayerLeftGameEvent extends GameEvent {

    private final GamePlayer player;

    public PlayerLeftGameEvent(Game game, GamePlayer player) {
        super(game);
        this.player = player;
    }

}
