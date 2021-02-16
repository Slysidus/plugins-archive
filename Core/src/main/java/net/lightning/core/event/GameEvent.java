package net.lightning.core.event;

import lombok.Getter;
import net.lightning.core.Game;

public abstract class GameEvent {

    @Getter
    private final Game game;

    protected GameEvent(Game game) {
        this.game = game;
    }

}
