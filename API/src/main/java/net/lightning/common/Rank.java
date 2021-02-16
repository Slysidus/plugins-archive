package net.lightning.common;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum Rank {

    PLAYER(ChatColor.GRAY, "Player", false),
    VIP(ChatColor.GREEN, "VIP"),

    HELPER(ChatColor.YELLOW, "Helper"),
    MODERATOR(ChatColor.GOLD, "Moderator"),

    DEVELOPER(ChatColor.LIGHT_PURPLE, "Developer"),
    ADMIN(ChatColor.RED, "Admin");

    private static final Map<String, Rank> BY_NAME;

    static {
        BY_NAME = new HashMap<>();
        for (Rank rank : values()) {
            BY_NAME.put(rank.name(), rank);
        }
    }

    private final ChatColor color;
    private final String displayName;
    private final boolean prefix;

    Rank(ChatColor color, String displayName, boolean prefix) {
        this.color = color;
        this.displayName = displayName;
        this.prefix = prefix;
    }

    Rank(ChatColor color, String displayName) {
        this(color, displayName, true);
    }

    public String getDisplayPrefix() {
        return prefix ? color + displayName + " " : color.toString();
    }

    public boolean isAboveOrEquals(Rank rank) {
        return this.ordinal() >= rank.ordinal();
    }

    public static Rank getRank(String name) {
        return BY_NAME.getOrDefault(name, Rank.PLAYER);
    }

}
