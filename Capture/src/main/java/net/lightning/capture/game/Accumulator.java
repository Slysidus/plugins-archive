package net.lightning.capture.game;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import net.lightning.core.GameTeam;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class Accumulator {

    private static final Random random = new Random();

    private final Location location;
    private final Set<Block> energyStorage;
    private final List<Location> strikesLocations;

    private final GameTeam team;
    @Setter
    private short power;

    public Accumulator(World world, Location location, Set<Location> energyStorage, List<Location> strikesLocations, GameTeam team, short power) {
        this.location = location;
        if (location.getWorld() == null) {
            location.setWorld(world);
        }

        for (Location powerUnitLocation : energyStorage) {
            if (powerUnitLocation.getWorld() == null) {
                powerUnitLocation.setWorld(world);
            }
        }
        this.energyStorage = energyStorage.stream()
                .map(Location::getBlock)
                .collect(Collectors.toSet());
        this.strikesLocations = strikesLocations;
        for (Location strikesLocation : strikesLocations) {
            if (strikesLocation.getWorld() == null) {
                strikesLocation.setWorld(world);
            }
        }

        this.team = team;
        this.power = power;
    }

    public List<Block> getPowerUnits(boolean powered) {
        return energyStorage.stream()
                .filter(block -> block.getType() == (powered ? Material.GLOWSTONE : Material.STAINED_CLAY))
                .collect(Collectors.toList());
    }

    public Block getRandomStorageUnit(boolean powered) {
        List<Block> powerUnits = getPowerUnits(powered);
        return powerUnits.isEmpty()
                ? null
                : powerUnits.get(random.nextInt(powerUnits.size()));
    }

    public void randomlyUpdatePowerUnits(boolean add) {
        Block powerUnit = getRandomStorageUnit(!add);
        if (powerUnit != null) {
            updateUnit(powerUnit, add);
        }
    }

    @SuppressWarnings("deprecation")
    public void updateUnit(Block powerUnit, boolean powered) {
        Preconditions.checkArgument(energyStorage.contains(powerUnit));
        powerUnit.setType(powered ? Material.GLOWSTONE : Material.STAINED_CLAY, false);
        if (!powered) {
            powerUnit.setData(DyeColor.BLACK.getData(), false);
        }
    }

}
