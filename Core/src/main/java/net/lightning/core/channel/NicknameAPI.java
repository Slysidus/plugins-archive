package net.lightning.core.channel;

import com.google.common.base.Preconditions;
import net.lightning.core.LightningGamePlugin;
import net.lightning.core.server.CraftOperationException;
import net.lightning.core.server.CraftServerHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

@Deprecated
public class NicknameAPI {

    private final LightningGamePlugin<?> plugin;

    private final Pattern allowedPattern = Pattern.compile("[a-zA-Z_]{3,16}");

    public NicknameAPI(LightningGamePlugin<?> plugin) {
        this.plugin = plugin;
    }

    public boolean isAllowed(String name) {
        return allowedPattern.matcher(name).find();
    }

    public boolean setNickname(Player nativePlayer, String name) {
        Preconditions.checkNotNull(name);
        if (!isAllowed(name)) {
            return false;
        }

        CraftServerHandler craftServerHandler = plugin.getCraftServerHandler();
        try {
            craftServerHandler.setFieldValue(craftServerHandler.getGameProfile(nativePlayer), "name", name);

            craftServerHandler.removeFromTablist(nativePlayer);
            craftServerHandler.addToTablist(nativePlayer);
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.hidePlayer(nativePlayer);
                onlinePlayer.showPlayer(nativePlayer);
            }
            return true;
        }
        catch (NoSuchFieldException | IllegalAccessException | CraftOperationException ex) {
            ex.printStackTrace();
        }
        return false;
    }

}
