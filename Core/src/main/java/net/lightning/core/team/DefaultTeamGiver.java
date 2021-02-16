package net.lightning.core.team;

import net.lightning.core.Game;
import net.lightning.core.GamePlayer;
import net.lightning.core.GameTeam;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultTeamGiver implements TeamGiver {

    @Override
    public GameTeam attribute(Game game, GamePlayer player) {
        int minPlayerCount = Arrays.stream(game.getTeams())
                .mapToInt(game::getPlayerCountInTeam)
                .min()
                .orElse(-1);
        if (minPlayerCount < 0) {
            return null;
        }

        List<GameTeam> possibleTeams = Arrays.stream(game.getTeams())
                .filter(team -> game.getPlayerCountInTeam(team) == minPlayerCount)
                .collect(Collectors.toList());
        return possibleTeams.get(game.getRandom().nextInt(possibleTeams.size()));
    }

}
