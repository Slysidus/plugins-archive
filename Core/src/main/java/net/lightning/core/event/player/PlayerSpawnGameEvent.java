package net.lightning.core.event.player;

import lombok.Getter;
import net.lightning.core.Game;
import net.lightning.core.GamePlayer;
import net.lightning.core.event.GameEvent;

@Getter
public class PlayerSpawnGameEvent extends GameEvent {

    private final GamePlayer player;

    public PlayerSpawnGameEvent(Game game, GamePlayer player) {
        super(game);
        this.player = player;
    }

}
