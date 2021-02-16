package net.lightning.core.sidebar;

import lombok.Getter;
import net.lightning.core.GameTeam;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

@Getter
public class SidebarPane {

    private @NotNull final Scoreboard nativeScoreboard;
    private @NotNull final Objective objective;

    private final Team[] lines;

    public SidebarPane(@NotNull Scoreboard nativeScoreboard, @NotNull Objective objective, int size) {
        this.nativeScoreboard = nativeScoreboard;
        this.objective = objective;
        this.lines = new Team[size];
    }

    public SidebarPane setDisplayName(String displayName) {
        objective.setDisplayName(displayName);
        return this;
    }

    public void prepareTeams(GameTeam[] teams) {
        for (GameTeam team : teams) {
            getNativeTeam(team);
        }
    }

    private Team registerNativeTeam(GameTeam team) {
        Team nativeTeam = nativeScoreboard.registerNewTeam(team.getName());
        nativeTeam.setPrefix(team.getPrefix() != null ?
                team.getColor() + team.getPrefix().substring(0, Math.min(14, team.getPrefix().length())) :
                team.getColor().toString());

        if (team.getSuffix() != null) {
            nativeTeam.setSuffix(team.getSuffix().substring(0, Math.min(16, team.getSuffix().length())));
        }

        if (!team.isAssignable()) {
            nativeTeam.setNameTagVisibility(NameTagVisibility.NEVER);
        }
        return nativeTeam;
    }

    public Team getNativeTeam(GameTeam team) {
        Team nativeTeam = nativeScoreboard.getTeam(team.getName());
        return nativeTeam != null ? nativeTeam : registerNativeTeam(team);
    }

    public void setLine(int line, String value) {
        if (line > lines.length || line < 0)
            return;

        if (value.length() > 16) {
            String initialFirstPart = value.substring(0, 16), initialSecondPart = value.substring(16);
            String firstPart = null, secondPart = null;

            int lastColorCharIndex = initialFirstPart.lastIndexOf('§');
            if (lastColorCharIndex + 1 == initialFirstPart.length()) {
                firstPart = initialFirstPart.substring(0, lastColorCharIndex);
                secondPart = '§' + initialSecondPart;
            }
            else if (lastColorCharIndex != -1) {
                ChatColor lastColor = ChatColor.getByChar(initialFirstPart.charAt(lastColorCharIndex + 1));
                if (lastColor != null) {
                    secondPart = lastColor + initialSecondPart;
                }
            }
            setLine(line, firstPart != null ? firstPart : initialFirstPart, secondPart != null ? secondPart : initialSecondPart);
        }
        else {
            setLine(line, value, null);
        }
    }

    private void setLine(int line, String part1, String part2) {
        if (line > lines.length || line < 0)
            return;

        Team team = getLineTeam(line);
        if (team.getEntries().isEmpty()) {
            final String key = getScoreEntry(line);
            team.addEntry(key);
            objective.getScore(key).setScore(lines.length - line);
        }

        team.setPrefix(part1 != null ? (part1.length() > 16 ? part1.substring(0, 16) : part1) : "");
        team.setSuffix(part2 != null ? (part2.length() > 16 ? part2.substring(0, 16) : part2) : "");
    }

    public void setScore(int line, int score) {
        objective.getScore(getScoreEntry(line)).setScore(score);
    }

    private String getScoreEntry(int line) {
        return ChatColor.values()[line].toString() + ChatColor.RESET;
    }

    private Team getLineTeam(int line) {
        if (lines[line] == null)
            lines[line] = nativeScoreboard.registerNewTeam("line" + nativeScoreboard.getTeams().size());
        return lines[line];
    }

}
