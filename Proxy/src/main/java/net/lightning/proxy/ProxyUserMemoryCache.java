package net.lightning.proxy;

import net.lightning.api.UserMemoryCache;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ProxyUserMemoryCache<V> extends UserMemoryCache<V> {

    public V get(ProxiedPlayer player) {
        return get(player.getUniqueId());
    }

    public V put(ProxiedPlayer player, V value) {
        return put(player.getUniqueId(), value);
    }

    public boolean isExcluded(ProxiedPlayer player) {
        return isExcluded(player.getUniqueId());
    }

}
