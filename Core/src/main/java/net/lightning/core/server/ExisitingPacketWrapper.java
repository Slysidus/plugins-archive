package net.lightning.core.server;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@Getter
@RequiredArgsConstructor
public class ExisitingPacketWrapper implements PacketWrapper {

    @Getter(AccessLevel.NONE)
    private final CraftServerHandler craftServerHandler;

    private final Object instance;

    @Override
    public void send(Player... receivers) {
        if (receivers.length == 0) {
            craftServerHandler.sendGlobalPacket(instance);
        }
        else {
            for (Player receiver : receivers) {
                craftServerHandler.sendPacket(receiver, instance);
            }
        }
    }

}
