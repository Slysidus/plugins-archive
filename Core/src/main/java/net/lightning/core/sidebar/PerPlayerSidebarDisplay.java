package net.lightning.core.sidebar;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import net.lightning.core.GamePlayer;
import net.lightning.core.GameTeam;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;

public class PerPlayerSidebarDisplay extends SidebarDisplay {

    private final Map<GamePlayer, SidebarPane> paneMap;
    private @NotNull final SidebarCreator creator;

    public PerPlayerSidebarDisplay(@NotNull SidebarCreator creator) {
        this.creator = creator;
        this.paneMap = Maps.newHashMap();
    }

    @Override
    public SidebarPane createDefault(GameTeam[] teams, int size) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("game", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        SidebarPane pane = new SidebarPane(scoreboard, objective, size);
        if (teams != null) {
            pane.prepareTeams(teams);
        }
        return pane;
    }

    @Override
    public SidebarPane getPane(GamePlayer player) {
        return paneMap.get(player);
    }

    @Override
    public SidebarPane addNewPlayer(GamePlayer player) {
        Preconditions.checkNotNull(player);

        SidebarPane pane = creator.createPane(player);
        if (pane == null) {
            return null;
        }
        paneMap.put(player, pane);
        return pane;
    }

    @Override
    public void removePlayer(GamePlayer player) {
        paneMap.remove(player);
    }

    @Override
    public Collection<GamePlayer> getPlayers() {
        return paneMap.keySet();
    }

    @Override
    public void setLine(int line, String value) {
        for (SidebarPane pane : paneMap.values()) {
            pane.setLine(line, value);
        }
    }

}
