package net.lightning.core.server.channel;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.Getter;
import net.lightning.core.server.CraftOperationException;
import net.lightning.core.server.CraftServerHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class PacketReaderInjector {

    private static final String CHANNEL_NAME = "lightning::packet_reader";

    private final CraftServerHandler craftServerHandler;

    @Getter
    private final Map<UUID, Channel> playerChannels;

    private final Multimap<Class<?>, PacketReader> readers;
    private Set<Class<?>> readersTypes;

    public PacketReaderInjector(CraftServerHandler craftServerHandler) {
        this.craftServerHandler = craftServerHandler;
        this.playerChannels = new HashMap<>();

        this.readers = LinkedListMultimap.create();
        this.readersTypes = Collections.emptySet();
    }

    public void inject(Player bukkitPlayer) throws CraftOperationException {
        try {
            Object playerConnection = craftServerHandler.getPlayerConnection(bukkitPlayer);
            Object playerNetworkManager = craftServerHandler.getFieldValue(playerConnection, "networkManager");

            Channel channel = (Channel) craftServerHandler.getFieldValue(playerNetworkManager, "channel");
            playerChannels.put(bukkitPlayer.getUniqueId(), channel);
            ChannelPipeline channelPipeline = channel.pipeline();

            if (channelPipeline.get(CHANNEL_NAME) != null) {
                return;
            }

            channelPipeline.addAfter("decoder", CHANNEL_NAME, new MessageToMessageDecoder<Object>() {
                @Override
                protected void decode(ChannelHandlerContext ctx, Object msg, List<Object> out) {
                    out.add(msg);
                    readPacket(bukkitPlayer, msg);
                }

                @Override
                public boolean acceptInboundMessage(Object msg) {
                    return readersTypes.contains(msg.getClass());
                }
            });
        }
        catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new CraftOperationException(ex);
        }
    }

    public void uninject(Player bukkitPlayer) {
        Channel channel = playerChannels.remove(bukkitPlayer.getUniqueId());
        if (channel != null) {
            ChannelPipeline channelPipeline = channel.pipeline();
            if (channelPipeline.get(CHANNEL_NAME) == null) {
                return;
            }
            channelPipeline.remove(CHANNEL_NAME);
        }
    }

    private void readPacket(Player bukkitPlayer, Object packet) {
        for (PacketReader reader : readers.get(packet.getClass())) {
            reader.read(bukkitPlayer, packet);
        }
    }

    public void registerReader(Class<?> packetClass, PacketReader packetReader) {
        if (!playerChannels.isEmpty()) {
            Bukkit.getLogger().warning("[Packet Reader] A packet reader has been registered while some players are already injected. Re-injecting them..");
            for (UUID uuid : playerChannels.keySet()) {
                Player bukkitPlayer = Bukkit.getPlayer(uuid);
                uninject(bukkitPlayer);
                inject(bukkitPlayer);
            }
        }

        readers.put(packetClass, packetReader);
        readersTypes = readers.keySet();
    }

}
