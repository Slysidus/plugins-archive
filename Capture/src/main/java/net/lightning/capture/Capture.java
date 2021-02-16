package net.lightning.capture;

import net.lightning.capture.game.CaptureGame;
import net.lightning.core.LightningGamePlugin;
import net.lightning.core.modules.AntiBuildModule;
import org.bukkit.Material;
import org.bukkit.util.Vector;

public final class Capture extends LightningGamePlugin<CaptureGame> {

    @Override
    public CaptureGame initGame() {
        saveResource("maps/default.yml", false);
        return new CaptureGame(this);
    }

    @Override
    public void destroyGame() {
        for (Vector vector : game.getModule(AntiBuildModule.class).getPlaceHistory()) {
            game.getWorld().getBlockAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ()).setType(Material.AIR);
        }
    }

}
