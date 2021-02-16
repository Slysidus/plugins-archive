package net.lightning.core.server;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.Collection;

@Getter
@RequiredArgsConstructor
public class MultiplePacketWrapper implements PacketWrapper {

    @Getter(AccessLevel.NONE)
    private final CraftServerHandler craftServerHandler;

    private final Collection<Object> packets;

    @Override
    public Object getInstance() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void send(Player... receivers) {
        if (receivers.length == 0) {
            for (Object packet : packets) {
                craftServerHandler.sendGlobalPacket(packet);
            }
        }
        else {
            for (Player receiver : receivers) {
                for (Object packet : packets) {
                    craftServerHandler.sendPacket(receiver, packet);
                }
            }
        }
    }

}
