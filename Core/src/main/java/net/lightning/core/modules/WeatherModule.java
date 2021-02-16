package net.lightning.core.modules;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.lightning.core.Game;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

@Getter
@Builder
public class WeatherModule extends GameModule<Game> implements Listener {

    private final String name = "Weather";

    @Setter
    private boolean disableRain;

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (!disableRain && !event.toWeatherState()) {
            return;
        }

        event.setCancelled(true);
        World world = event.getWorld();
        if (world.isThundering()) {
            event.getWorld().setThunderDuration(0);
            event.getWorld().setThundering(false);
        }
    }

    public static class WeatherModuleBuilder {

        WeatherModuleBuilder() {
            disableRain = true;
        }

    }

}
