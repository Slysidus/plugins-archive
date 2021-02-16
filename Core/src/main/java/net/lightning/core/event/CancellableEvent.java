package net.lightning.core.event;

import lombok.Getter;
import lombok.Setter;
import net.lightning.core.Game;

public abstract class CancellableEvent extends GameEvent {

    @Getter
    @Setter
    private boolean cancelled;

    protected CancellableEvent(Game game, boolean cancelled) {
        super(game);
        this.cancelled = cancelled;
    }

}
