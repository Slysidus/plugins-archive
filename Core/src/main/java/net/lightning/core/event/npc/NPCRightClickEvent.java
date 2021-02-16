package net.lightning.core.event.npc;

import lombok.Getter;
import net.lightning.core.Game;
import net.lightning.core.event.CancellableEvent;
import net.lightning.core.server.PlayerNPC;

@Getter
public class NPCRightClickEvent extends CancellableEvent {

    private final PlayerNPC npc;

    public NPCRightClickEvent(Game game, PlayerNPC npc) {
        super(game, false);
        this.npc = npc;
    }

}
