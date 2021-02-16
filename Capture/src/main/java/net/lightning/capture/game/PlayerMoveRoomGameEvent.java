package net.lightning.capture.game;

import lombok.Getter;
import net.lightning.core.Game;
import net.lightning.core.event.CancellableEvent;

@Getter
public class PlayerMoveRoomGameEvent extends CancellableEvent {

    private final CaptureGamePlayer player;
    private final CaptureRoom from, to;

    public PlayerMoveRoomGameEvent(Game game, CaptureGamePlayer player, CaptureRoom to) {
        super(game, false);
        this.player = player;
        this.from = player.getCurrentRoom();
        this.to = to;
    }

}
