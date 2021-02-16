package net.lightning.core.event.player;

import lombok.Getter;
import lombok.Setter;
import net.lightning.core.Game;
import net.lightning.core.GamePlayer;
import net.lightning.core.event.GameEvent;

@Getter
@Setter
public class PlayerJoinGameEvent extends GameEvent {

    private final GamePlayer player;

    private boolean callSpawnEvent;

    public PlayerJoinGameEvent(Game game, GamePlayer player) {
        super(game);
        this.player = player;
        this.callSpawnEvent = true;
    }

}
