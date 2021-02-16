package net.lightning.lobby;

import net.lightning.common.Rank;
import net.lightning.core.GamePlayer;
import net.lightning.core.LightningGamePlugin;
import net.lightning.lobby.commands.GriefCommand;
import net.lightning.lobby.commands.SpawnCommand;

public final class Lobby extends LightningGamePlugin<LobbyGame> {

    @Override
    public LobbyGame initGame() {
        return new LobbyGame(this);
    }

    @Override
    public void postInit() {
        registerCommands(
                new SpawnCommand(this),

                new GriefCommand(this)
        );
    }

    @Override
    public void destroyGame() {

    }

    @Override
    public void onPlayerRankUpdate(GamePlayer gamePlayer, Rank previousRank) {
        Rank newRank = gamePlayer.getNetworkPlayer().getRank();

        game.setTeam(gamePlayer, game.getTeamsByRank().get(newRank));
        game.updateSidebar(game.getSidebarDisplay().getPane(gamePlayer), gamePlayer);

        gamePlayer.getNativePlayer().setAllowFlight(newRank.isAboveOrEquals(Rank.VIP));
    }

}
