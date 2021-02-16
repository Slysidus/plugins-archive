package net.lightning.core.event.player;

import lombok.Getter;
import lombok.Setter;
import net.lightning.core.Game;
import net.lightning.core.GamePlayer;
import net.lightning.core.event.CancellableEvent;
import org.jetbrains.annotations.NotNull;

@Getter
public class PlayerPreJoinGameEvent extends CancellableEvent {

    private final GamePlayer player;

    @Setter
    @NotNull
    private String message;

    public PlayerPreJoinGameEvent(Game game, GamePlayer player) {
        super(game, false);
        this.player = player;
        this.message = "";
    }

}
