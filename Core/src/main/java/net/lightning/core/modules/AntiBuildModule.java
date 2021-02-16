package net.lightning.core.modules;

import com.google.common.collect.Sets;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;
import net.lightning.core.Game;
import net.lightning.core.GamePlayer;
import net.lightning.core.GameState;
import net.lightning.core.map.GameMapModel;
import net.lightning.core.modules.events.AntiBuildDenyEvent;
import net.lightning.core.util.ArrayPool;
import net.lightning.core.world.Zone;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;

@Getter
@Setter
public class AntiBuildModule extends GameModule<Game> implements Listener {

    private final String name = "AntiBuild";

    private final boolean keepHistory;
    private final List<Vector> placeHistory;

    private boolean checkGameState, cannotBreakUnplaced;
    private int maxBuildHeight;

    private final List<Zone> protectedZones;
    private final ArrayPool.BiPredicatePool<GamePlayer, Block> placeChecks, breakChecks;

    @Builder
    public AntiBuildModule(boolean checkGameState,
                           boolean keepHistory,
                           boolean cannotBreakUnplaced,
                           int maxBuildHeight,
                           @Singular List<Zone> protectedZones,
                           @Singular List<BiPredicate<GamePlayer, Block>> placeChecks,
                           @Singular List<BiPredicate<GamePlayer, Block>> breakChecks) {
        this.checkGameState = checkGameState;
        this.keepHistory = keepHistory;
        this.cannotBreakUnplaced = cannotBreakUnplaced;
        this.maxBuildHeight = maxBuildHeight;
        this.protectedZones = protectedZones;
        this.placeChecks = new ArrayPool.BiPredicatePool<>(placeChecks);
        this.breakChecks = new ArrayPool.BiPredicatePool<>(breakChecks);

        this.placeHistory = keepHistory ? new ArrayList<>() : null;
    }

    @Override
    public void setGame(Game game) {
        super.setGame(game);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockPlaceLow(BlockPlaceEvent event) {
        placeEventCheck(event, event.getPlayer(), event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlaceMonitor(BlockPlaceEvent event) {
        if (keepHistory) {
            placeHistory.add(event.getBlock().getLocation().toVector());
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        breakEventCheck(event, event.getPlayer(), event.getBlock());
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Block block = event.getBlockClicked().getRelative(event.getBlockFace());
        placeEventCheck(event, event.getPlayer(), block);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBucketEmtpyMonitor(PlayerBucketEmptyEvent event) {
        Block block = event.getBlockClicked().getRelative(event.getBlockFace());
        if (keepHistory) {
            placeHistory.add(block.getLocation().toVector());
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        Block block = event.getBlockClicked().getRelative(event.getBlockFace());
        breakEventCheck(event, event.getPlayer(), block);
    }

    private void placeEventCheck(Cancellable event, Player nativePlayer, Block block) {
        GamePlayer player = game.getPlayer(nativePlayer);
        if (checkGameState && game.getGameState() != GameState.PLAYING) {
            handleEventCancellation(event, AntiBuildDenyReason.GAME_STATE, player, block);
            return;
        }

        GameMapModel map = game.getMap();
        if (map.bounds != null && !map.bounds.contains(block)) {
            handleEventCancellation(event, AntiBuildDenyReason.OUT_OF_BOUNDS, player, block);
            return;
        }

        if (!placeChecks.testAll(player, block)
                || (maxBuildHeight > 0 && block.getY() > maxBuildHeight)) {
            handleEventCancellation(event, AntiBuildDenyReason.DENIED_PLACE, player, block);
            return;
        }

        for (Zone protectedZone : protectedZones) {
            if (protectedZone.contains(block)) {
                handleEventCancellation(event, AntiBuildDenyReason.DENIED_PLACE, player, block);
                return;
            }
        }
    }

    private void breakEventCheck(Cancellable event, Player nativePlayer, Block block) {
        GamePlayer player = game.getPlayer(nativePlayer);
        if (checkGameState && game.getGameState() != GameState.PLAYING) {
            handleEventCancellation(event, AntiBuildDenyReason.GAME_STATE, player, block);
            return;
        }

        if (!placeChecks.testAll(player, block)) {
            handleEventCancellation(event, AntiBuildDenyReason.DENIED_BREAK, player, block);
        }

        if (keepHistory && cannotBreakUnplaced
                && !placeHistory.contains(block.getLocation().toVector())) {
            handleEventCancellation(event, AntiBuildDenyReason.HISTORY_MISMATCH, player, block);
        }
    }

    private void handleEventCancellation(Cancellable event, AntiBuildDenyReason reason, GamePlayer player, Block block) {
        event.setCancelled(true);
        game.getEventManager().fireEvent(new AntiBuildDenyEvent(game, player, block, reason));
    }

    public enum AntiBuildDenyReason {
        GAME_STATE,
        DENIED_PLACE,
        OUT_OF_BOUNDS,
        DENIED_BREAK,
        HISTORY_MISMATCH;

        public boolean isDenied() {
            return this.ordinal() >= 1;
        }
    }

    public static class AntiBuildModuleBuilder {

        public AntiBuildModuleBuilder materialPlaceCheck(Material... materials) {
            final Set<Material> allowedBlocks = Sets.newHashSet(materials);
            placeCheck((player, block) -> allowedBlocks.contains(block.getType()));
            return this;
        }

        public AntiBuildModuleBuilder materialBreakCheck(Material... materials) {
            final Set<Material> allowedBlocks = Sets.newHashSet(materials);
            breakCheck((player, block) -> allowedBlocks.contains(block.getType()));
            return this;
        }

        public AntiBuildModuleBuilder materialDoubleCheck(Material... materials) {
            final Set<Material> allowedBlocks = Sets.newHashSet(materials);
            placeCheck((player, block) -> allowedBlocks.contains(block.getType()));
            breakCheck((player, block) -> allowedBlocks.contains(block.getType()));
            return this;
        }

        public AntiBuildModuleBuilder placeBreakCheck(BiPredicate<GamePlayer, Block> biPredicate) {
            placeCheck(biPredicate);
            breakCheck(biPredicate);
            return this;
        }

    }

}
