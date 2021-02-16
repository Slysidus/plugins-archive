package net.lightning.core.server;

import org.bukkit.entity.Player;

public interface PacketWrapper {

    Object getInstance();

    void send(Player... receivers);

}
