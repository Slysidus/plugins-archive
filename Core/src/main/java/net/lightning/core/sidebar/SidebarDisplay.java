package net.lightning.core.sidebar;

import net.lightning.core.GamePlayer;
import net.lightning.core.GameTeam;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Consumer;

public abstract class SidebarDisplay {

    public abstract SidebarPane createDefault(@Nullable GameTeam[] teams, int size);

    public abstract SidebarPane getPane(GamePlayer player);

    public abstract SidebarPane addNewPlayer(GamePlayer player);

    public abstract void removePlayer(GamePlayer player);

    public abstract Collection<GamePlayer> getPlayers();

    public void apply(Collection<GamePlayer> players) {
        for (GamePlayer player : players) {
            SidebarPane pane = getPane(player);
            if (pane != null) {
                player.getNativePlayer().setScoreboard(pane.getNativeScoreboard());
            }
        }
    }

    public void forTeam(GameTeam team, Consumer<Team> nativeTeamAction) {
        for (GamePlayer player : getPlayers()) {
            SidebarPane pane = getPane(player);
            if (pane != null) {
                Team nativeTeam = pane.getNativeTeam(team);
                if (nativeTeam != null) {
                    nativeTeamAction.accept(nativeTeam);
                }
            }
        }
    }

    /*
    Actions executed on each side pane
     */

    public abstract void setLine(int line, String value);

}
