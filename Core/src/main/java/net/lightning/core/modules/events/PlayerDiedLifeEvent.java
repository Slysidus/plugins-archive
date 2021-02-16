package net.lightning.core.modules.events;

import lombok.Getter;
import lombok.Setter;
import net.lightning.core.Game;
import net.lightning.core.GamePlayer;
import net.lightning.core.event.GameEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEvent;

@Getter
public class PlayerDiedLifeEvent extends GameEvent {

    private final GamePlayer player;
    private final EntityEvent eventSource;

    @Setter
    private boolean keepInventory, keepExp;

    public PlayerDiedLifeEvent(Game game, GamePlayer player, EntityEvent eventSource, boolean keepInventory, boolean keepExp) {
        super(game);
        this.player = player;
        this.eventSource = eventSource;
        this.keepInventory = keepInventory;
        this.keepExp = keepExp;
    }

}
