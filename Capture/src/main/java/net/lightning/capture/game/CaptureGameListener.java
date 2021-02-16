package net.lightning.capture.game;

import net.lightning.core.GamePlayer;
import net.lightning.core.GameTeam;
import net.lightning.core.util.WorldUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.Map;

public class CaptureGameListener implements Listener {

    private final CaptureGame game;

    public CaptureGameListener(CaptureGame game) {
        this.game = game;
    }

    // Steal energy
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.GLOWSTONE) {
            GamePlayer player = game.getPlayer(event.getPlayer());
            Accumulator targetAccumulator = game.getTeamToAccumulator().values().stream()
                    .filter(accumulator -> WorldUtil.naiveEqualsCheck(accumulator.getLocation(), event.getBlock().getLocation()))
                    .findAny().orElse(null);

            if (targetAccumulator == null) {
                return;
            }

            event.setCancelled(true);
            GameTeam team = player.getTeam();
            if (team.equals(targetAccumulator.getTeam())) {
                player.sendUnlocalizedMessage(ChatColor.RED + "You cannot steal energy from your own accumulator.");
                return;
            }

            player.naiveTeleport(game.getTeamToModel().get(team).spawnLocation);
            if (game.stealPower(team, targetAccumulator.getTeam(), (short) 10)) {
                Accumulator teamAccumulator = game.getTeamToAccumulator().get(team);
                strike(targetAccumulator, false);
                strike(teamAccumulator, true);

                game.broadcast(team.getColor() + player.getName() + ChatColor.GRAY + " has stolen " + ChatColor.YELLOW + "10% of energy" + ChatColor.GRAY + ".");
                if (teamAccumulator.getPower() == 100) {
                    game.win(teamAccumulator);
                }
            }
        }
    }

    private void strike(Accumulator accumulator, boolean add) {
        accumulator.getLocation().getWorld().strikeLightningEffect(accumulator.getLocation());
        accumulator.randomlyUpdatePowerUnits(add);
    }

    // Teleporters
    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (event.getPlayer().isSneaking()) {
            Player nativePlayer = event.getPlayer();
            CaptureGamePlayer player = (CaptureGamePlayer) game.getPlayer(nativePlayer);

            Location blockBelow = nativePlayer.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation();
            Map.Entry<Location, CaptureRoom> teleporter = game.getTeamToModel().get(player.getTeam()).getMatchingTeleporter(blockBelow);
            if (teleporter != null) {
                Location teleporterLocation = teleporter.getKey();
                CaptureRoom movingTo = teleporter.getValue();

                PlayerMoveRoomGameEvent gameEvent = new PlayerMoveRoomGameEvent(game, player, movingTo);
                game.getEventManager().fireEvent(gameEvent);
                if (gameEvent.isCancelled()) {
                    return;
                }

                player.naiveTeleport(new Location(
                        nativePlayer.getWorld(),
                        teleporterLocation.getX() + .5,
                        teleporterLocation.getY() + 1.1,
                        teleporterLocation.getZ() + .5,
                        nativePlayer.getLocation().getYaw(),
                        nativePlayer.getLocation().getPitch()));
                player.setCurrentRoom(movingTo);
                nativePlayer.playSound(nativePlayer.getLocation(), Sound.ENDERMAN_TELEPORT, 1f, 0);
            }
        }
    }

}
