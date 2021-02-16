package net.lightning.core;

import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

@Getter
public class GameTeam {

    /**
     * Temporary UUID assigned to the team for the current game.
     */
    private final @NotNull UUID uniqueId;
    private final @NotNull String name;
    private final @Nullable String prefix, suffix;
    private final @NotNull ChatColor color;
    private final boolean assignable;

    @Builder
    protected GameTeam(@Nullable UUID uniqueId,
                       @Nullable String name,
                       @Nullable String prefix,
                       @Nullable String suffix,
                       @NotNull ChatColor color,
                       int index,
                       boolean assignable) {
        Preconditions.checkNotNull(color);

        if (uniqueId == null) {
            uniqueId = UUID.randomUUID();
        }
        if (name == null) {
            name = prefix != null ? prefix.trim() : color.name();
        }

        this.uniqueId = uniqueId;
        this.name = name;
        this.prefix = prefix;
        this.suffix = suffix;
        this.color = color;
        this.index = index;
        this.assignable = assignable;
    }

    /**
     * Index in teams[] array
     */
    @Accessors(fluent = true)
    private final int index;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameTeam)) return false;
        GameTeam gameTeam = (GameTeam) o;
        return index == gameTeam.index &&
                uniqueId.equals(gameTeam.uniqueId) &&
                name.equals(gameTeam.name) &&
                Objects.equals(prefix, gameTeam.prefix) &&
                Objects.equals(suffix, gameTeam.suffix) &&
                color == gameTeam.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId, name, prefix, suffix, color, index);
    }

    public static class GameTeamBuilder {

        GameTeamBuilder() {
            assignable = true;
        }

    }

}
