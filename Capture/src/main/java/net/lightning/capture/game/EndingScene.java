package net.lightning.capture.game;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.lightning.core.Game;
import net.lightning.core.GamePlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EndingScene extends BukkitRunnable {

    private final Accumulator accumulator;
    private final List<Integer> spreadIndexes;
    private final Multimap<Integer, Block> spreadBlocks;

    private Collection<GamePlayer> players;
    private Consumer<List<GamePlayer>> callback;

    private boolean playing;
    private int strikesCombo;

    public EndingScene(Accumulator accumulator, int radius, int height) throws IllegalStateException {
        this.accumulator = accumulator;
        Location fireStart = accumulator.getLocation().clone();
        while (fireStart.getBlock().getType() != Material.COAL_BLOCK && fireStart.getY() > 1) {
            fireStart.setY(fireStart.getY() - 1);
        }

        Block fireStartBlock = fireStart.getBlock();
        if (fireStartBlock == null) {
            throw new IllegalStateException();
        }

        List<Block> coalBlocks = new ArrayList<>();
        for (int x = -radius; x < radius; x++) {
            for (int y = -height; y < height; y++) {
                for (int z = -radius; z < radius; z++) {
                    Block block = fireStartBlock.getRelative(x, y, z);
                    if (block.getType() == Material.COAL_BLOCK) {
                        coalBlocks.add(block);
                    }
                }
            }
        }

        Multimap<Integer, Block> filteredCoalBlocks = HashMultimap.create();
        filteredCoalBlocks.put(-1, fireStartBlock);
        for (Block coalBlock : coalBlocks) {
            filteredCoalBlocks.put((int) coalBlock.getLocation().distance(fireStartBlock.getLocation()), coalBlock);
        }

        List<Integer> orderedCoalDistances = new ArrayList<>(filteredCoalBlocks.keySet());
        Collections.sort(orderedCoalDistances);
        this.spreadIndexes = orderedCoalDistances;
        this.spreadBlocks = filteredCoalBlocks;
    }

    public void play(Collection<GamePlayer> players, int delay, Consumer<List<GamePlayer>> callback) {
        Preconditions.checkArgument(!playing);
        this.players = players;
        this.callback = callback;

        if (players.isEmpty()) {
            return;
        }

        Game game = players.iterator().next().getGame();
        super.runTaskTimer(game.getPlugin(), 0, delay);
        this.playing = true;
    }

    @Override
    public void run() {
        if (strikesCombo > 0) {
            if (strikesCombo++ == 3) {
                cancel();
                return;
            }

            for (Location strikesLocation : accumulator.getStrikesLocations()) {
                strikesLocation.getWorld().strikeLightningEffect(strikesLocation);
            }
        }
        else if (spreadIndexes.isEmpty()) {
            for (Location strikesLocation : accumulator.getStrikesLocations()) {
                strikesLocation.getWorld().strikeLightningEffect(strikesLocation);
            }
            strikesCombo++;
            callback.accept(players.stream()
                    .filter(GamePlayer::isOnline)
                    .collect(Collectors.toList()));
        }
        else {
            int spreadIndex = spreadIndexes.remove(0);
            Collection<Block> blocks = spreadBlocks.get(spreadIndex);
            for (Block block : blocks) {
                block.setType(Material.GLOWSTONE);
            }

            players.forEach(player -> player.playSound(Sound.NOTE_STICKS));
        }
    }

}
