package net.lightning.api;

import com.google.common.collect.ForwardingMap;

import java.util.*;

public class UserMemoryCache<V> extends ForwardingMap<UUID, V> {

    private final Map<UUID, V> cacheMap;
    private final List<UUID> excludedUsers;

    public UserMemoryCache() {
        this.cacheMap = new HashMap<>();
        this.excludedUsers = new ArrayList<>();
    }

    public boolean isExcluded(UUID user) {
        return !cacheMap.containsKey(user) && excludedUsers.contains(user);
    }

    public void exclude(UUID user) {
        excludedUsers.add(user);
    }

    @Override
    protected Map<UUID, V> delegate() {
        return cacheMap;
    }

}
