package net.lightning.lobby;

import lombok.Getter;
import lombok.Setter;
import net.lightning.common.models.NetworkPlayer;
import net.lightning.core.Game;
import net.lightning.core.GamePlayer;
import org.bukkit.entity.Player;

@Getter
@Setter
public class LobbyPlayer extends GamePlayer {

    private boolean hidePlayers;
    private boolean griefer;

    public LobbyPlayer(Game game, Player nativePlayer, NetworkPlayer networkPlayer) {
        super(game, nativePlayer, networkPlayer);
    }

}
