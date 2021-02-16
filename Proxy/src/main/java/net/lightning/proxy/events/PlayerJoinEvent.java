package net.lightning.proxy.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lightning.common.models.NetworkPlayer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

@Getter
@AllArgsConstructor
public class PlayerJoinEvent extends Event {

    private final ProxiedPlayer proxiedPlayer;
    private final NetworkPlayer player;

}
