package net.lightning.core.server.channel;

import org.bukkit.entity.Player;

public interface PacketReader {

    void read(Player bukkitPlayer, Object packet);

}
