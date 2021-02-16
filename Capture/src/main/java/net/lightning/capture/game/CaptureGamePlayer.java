package net.lightning.capture.game;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.lightning.common.models.NetworkPlayer;
import net.lightning.core.Game;
import net.lightning.core.GamePlayer;
import net.lightning.core.stats.FightGameStats;
import org.bukkit.entity.Player;

@Getter
@Setter
@ToString(callSuper = true)
public class CaptureGamePlayer extends GamePlayer {

    private CaptureKit selectedKit;
    private CaptureRoom currentRoom;

    protected CaptureGamePlayer(Game game, Player player, NetworkPlayer networkPlayer) {
        super(game, player, networkPlayer);
        this.selectedKit = CaptureKit.SOLDIER;
        this.currentRoom = CaptureRoom.OUTSIDE;

        this.gameStats = new FightGameStats();
    }

}
