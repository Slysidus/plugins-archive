package net.lightning.common.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lightning.api.database.model.CachableModel;
import net.lightning.api.database.model.Model;
import net.lightning.api.database.model.ModelField;
import net.lightning.api.util.StringUtil;
import net.lightning.common.Rank;
import net.md_5.bungee.api.ChatColor;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Model(tableName = "players")
@NoArgsConstructor
@AllArgsConstructor
public class NetworkPlayer implements CachableModel {

    @ModelField(unique = true, primaryKey = true)
    private UUID uniqueId;
    @ModelField(fieldName = "username", unique = true)
    private String cachedName;

    @ModelField
    private Rank rank;

    @ModelField
    private boolean online;
    @ModelField
    private Timestamp lastLogin;
    @ModelField
    private Timestamp registrationDate;

    @ModelField
    private int prestige;
    @ModelField
    private long prestigeExp;

    @ModelField
    private int coins;

    public String getPrestigeDisplay() {
        if (prestige < 1) {
            return ChatColor.GRAY + "Ø";
        }

        return getPrestigeColor() + StringUtil.numberToRoman(prestige);
    }

    public ChatColor getPrestigeColor() {
        if (prestige < 5) {
            return ChatColor.LIGHT_PURPLE;
        }
        else if (prestige < 10) {
            return ChatColor.DARK_PURPLE;
        }
        else if (prestige < 15) {
            return ChatColor.WHITE;
        }
        return ChatColor.GRAY;
    }

    public long getRequiredPrestigeExp() {
        return (long) 2e3;
    }

    public boolean hasRankOrAbove(Rank rank) {
        return this.rank.isAboveOrEquals(rank);
    }

    @Override
    public String getCacheKey() {
        return uniqueId.toString();
    }

}
