package net.lightning.core.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Location;

@UtilityClass
public class WorldUtil {

    public boolean naiveEqualsCheck(Location location1, Location location2) {
        if (location1 == null || location2 == null) {
            return false;
        }

        if (Double.doubleToLongBits(location1.getX()) != Double.doubleToLongBits(location2.getX())) {
            return false;
        }
        if (Double.doubleToLongBits(location1.getY()) != Double.doubleToLongBits(location2.getY())) {
            return false;
        }
        if (Double.doubleToLongBits(location1.getZ()) != Double.doubleToLongBits(location2.getZ())) {
            return false;
        }
        return true;
    }

}
