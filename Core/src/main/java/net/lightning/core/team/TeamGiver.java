package net.lightning.core.team;

import net.lightning.core.Game;
import net.lightning.core.GamePlayer;
import net.lightning.core.GameTeam;

public interface TeamGiver {

    GameTeam attribute(Game game, GamePlayer player);

}
