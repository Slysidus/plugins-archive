package net.lightning.core;

import lombok.Data;
import lombok.ToString;
import net.lightning.common.models.NetworkPlayer;
import net.lightning.core.event.player.PlayerSpawnGameEvent;
import net.lightning.core.graphics.ContainerGUI;
import net.lightning.core.server.CraftServerHandler;
import net.lightning.core.stats.GameStats;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

@Data
@ToString(callSuper = true)
public class GamePlayer {

    private final Game game;
    private final Player nativePlayer;
    private final NetworkPlayer networkPlayer;

    protected GameStats gameStats;
    protected GameTeam team;

    /*
    Quality Of Life
     */

    public UUID getUniqueId() {
        return nativePlayer.getUniqueId();
    }

    public String getName() {
        return nativePlayer.getName();
    }

    public boolean isOnline() {
        return nativePlayer.isOnline();
    }

    public boolean hasDisconnected() {
        return !nativePlayer.isOnline();
    }

    public void playSound(Sound sound) {
        nativePlayer.playSound(nativePlayer.getLocation(), sound, 1f, 0);
    }

    public void hide(GamePlayer target) {
        nativePlayer.hidePlayer(target.getNativePlayer());
    }

    public void hideAll() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            nativePlayer.hidePlayer(onlinePlayer);
        }
    }

    public void show(GamePlayer target) {
        nativePlayer.showPlayer(target.getNativePlayer());
    }

    public void showAll() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            nativePlayer.showPlayer(onlinePlayer);
        }
    }

    public boolean canSee(GamePlayer target) {
        return nativePlayer.canSee(target.getNativePlayer());
    }

    public LightningGamePlugin<?> getPlugin() {
        return game.getPlugin();
    }

    public CraftServerHandler getCraftServerHandler() {
        return game.getPlugin().getCraftServerHandler();
    }

    /*
    Event QoL
     */

    public void triggerSpawnEvent() {
        game.getEventManager().fireEvent(new PlayerSpawnGameEvent(game, this));
    }

    /*
    Core based actions
     */

    public void naiveTeleport(@NotNull Location location) {
        if (location.getWorld() == null) {
            location = new Location(
                    nativePlayer.getWorld(),
                    location.getX(),
                    location.getY(),
                    location.getZ(),
                    location.getYaw(),
                    location.getPitch());
        }
        nativePlayer.teleport(location);
    }

    public void openGUI(@NotNull ContainerGUI containerGUI) {
        nativePlayer.openInventory(containerGUI.getInventory());
    }

    public @Nullable ContainerGUI getOpenGUI() {
        if (nativePlayer.getOpenInventory().getTopInventory() != null) {
            Inventory topInventory = nativePlayer.getOpenInventory().getTopInventory();
            if (topInventory.getHolder() instanceof ContainerGUI) {
                return (ContainerGUI) topInventory.getHolder();
            }
        }
        return null;
    }

    public void sendTitle(@Nullable String title, @Nullable String subTitle) {
        sendTitle(title, subTitle, 0, 0, 0);
    }

    public void sendTitle(@Nullable String title, @Nullable String subTitle, int duration) {
        int fade = duration / 4;
        sendTitle(title, subTitle, fade, duration, fade);
    }

    public void sendTitle(@Nullable String title, @Nullable String subTitle, int fadeIn, int duration, int fadeOut) {
        getCraftServerHandler().packet_sendTitle(title, subTitle, fadeIn, duration, fadeOut)
                .send(nativePlayer);
    }

    public void clearTitle() {
        sendTitle("", "", 0, 0, 0);
    }

    /*
    Internationalization
     */

    public void sendUnlocalizedMessage(String message) {
        nativePlayer.sendMessage(message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof GamePlayer)) return false;
        GamePlayer that = (GamePlayer) o;
        return Objects.equals(game, that.game) &&
                Objects.equals(nativePlayer, that.nativePlayer) &&
                Objects.equals(team, that.team);
    }

    @Override
    public int hashCode() {
        return nativePlayer.hashCode();
    }

}
