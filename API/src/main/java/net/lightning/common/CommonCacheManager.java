package net.lightning.common;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Getter;
import net.lightning.api.database.model.ModelAccessor;
import net.lightning.api.database.model.ModelManager;
import net.lightning.common.models.NetworkPlayer;
import net.lightning.common.models.stats.INetworkStats;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;

import java.util.HashMap;
import java.util.Map;

@Getter
public class CommonCacheManager {

    private final RedissonClient redissonClient;

    private final ModelAccessor<NetworkPlayer>.RedisAccessorContext playerCache;

    @Getter(AccessLevel.NONE)
    private final Map<Class<? extends INetworkStats>, ModelAccessor<? extends INetworkStats>.RedisAccessorContext> statsCaches;

    public CommonCacheManager(RedissonClient redissonClient, ModelAccessor<NetworkPlayer>.RedisAccessorContext playerCache) {
        this.redissonClient = redissonClient;
        this.playerCache = playerCache;
        this.statsCaches = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <T extends INetworkStats> T getStats(Class<T> statsClass, NetworkPlayer networkPlayer) {
        Preconditions.checkNotNull(networkPlayer);
        if (!statsCaches.containsKey(statsClass)) {
            statsCaches.put(statsClass, ModelManager.getAccessor(statsClass).new RedisAccessorContext(redissonClient));
        }

        ModelAccessor<T>.RedisAccessorContext cache = (ModelAccessor<T>.RedisAccessorContext) statsCaches.get(statsClass);
        if (cache == null) {
            return null;
        }

        T stats = null;
        int tries = 0;
        while (stats == null && tries++ < 5) {
            stats = cache.get(networkPlayer.getUniqueId());
        }
        return stats;
    }

    public static RedissonClient createClient(String host,
                                              int port,
                                              String password,
                                              int database,
                                              boolean useNativeEpoll,
                                              int threads) {
        Config redisConfig = new Config();
        if (useNativeEpoll) {
            redisConfig.setUseLinuxNativeEpoll(true);
        }

        redisConfig.setThreads(threads);
        redisConfig.setNettyThreads(threads);
        SingleServerConfig singleServerConfig = redisConfig.useSingleServer()
                .setAddress(host + ":" + port)
                .setDatabase(database);

        if (password != null) {
            singleServerConfig.setPassword(password);
        }

        return Redisson.create(redisConfig);
    }

    public static CommonCacheManager createCacheManager(RedissonClient redissonClient) {
        return new CommonCacheManager(
                redissonClient,
                ModelManager.getAccessor(NetworkPlayer.class).new RedisAccessorContext(redissonClient)
        );
    }

}
