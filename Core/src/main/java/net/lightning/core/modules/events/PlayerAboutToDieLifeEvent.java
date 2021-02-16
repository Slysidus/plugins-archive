package net.lightning.core.modules.events;

import lombok.Getter;
import lombok.Setter;
import net.lightning.core.Game;
import net.lightning.core.GamePlayer;
import net.lightning.core.event.GameEvent;
import org.bukkit.event.entity.EntityDamageEvent;

@Getter
public class PlayerAboutToDieLifeEvent extends GameEvent {

    private final GamePlayer player;

    @Setter
    private Result result;
    private final EntityDamageEvent eventSource;

    public PlayerAboutToDieLifeEvent(Game game, GamePlayer player, EntityDamageEvent eventSource) {
        super(game);
        this.player = player;
        this.result = Result.DIE;
        this.eventSource = eventSource;
    }

    public enum Result {

        DIE,
        CANCEL_FATAL_HIT,
        FAKE_DEATH

    }

}
