package net.lightning.core.modules;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.lightning.core.Game;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

@Getter
@Builder
public class FoodRegulatorModule extends GameModule<Game> implements Listener {

    private final String name = "FoodRegulator";

    @Setter
    private boolean infiniteFood;

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) {
            return;
        }

        Player player = (Player) event.getEntity();
        if (infiniteFood) {
            event.setFoodLevel(20);
            player.setSaturation(20F);
        }
    }

    /*
    FIXME:
    - Eat
    - Potion
    - Milk bucket
    Modes whitelist/blacklist
     */

}
