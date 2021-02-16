package net.lightning.core.channel;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import lombok.AllArgsConstructor;
import net.lightning.common.Rank;
import net.lightning.core.GamePlayer;
import net.lightning.core.LightningGamePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.UUID;

@AllArgsConstructor
public class LightningChannelListener implements PluginMessageListener {

    private final LightningGamePlugin<?> plugin;

    @Override
    public void onPluginMessageReceived(String channel, Player ignored, byte[] message) {
        if (!channel.equals("Lightning")) {
            throw new IllegalStateException();
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        Player player = Bukkit.getPlayer(UUID.fromString(in.readUTF()));
        if (player == null) {
            throw new IllegalStateException();
        }

        switch (in.readUTF()) {
            case "RankUpdate":
                GamePlayer gamePlayer = plugin.getGame().getPlayers().get(player.getUniqueId());
                if (gamePlayer == null) {
                    if (!plugin.getGame().getSyncingPlayers().contains(player.getUniqueId())) {
                        throw new IllegalStateException();
                    }
                    return;
                }

                Rank previousRank = gamePlayer.getNetworkPlayer().getRank();
                gamePlayer.getNetworkPlayer().setRank(Rank.values()[in.readInt()]);
                plugin.onPlayerRankUpdate(gamePlayer, previousRank);
                break;
        }
    }

}
