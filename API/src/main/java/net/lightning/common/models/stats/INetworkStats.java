package net.lightning.common.models.stats;

import net.lightning.api.database.model.CachableModel;

import java.util.UUID;

public interface INetworkStats extends CachableModel {

    UUID getPlayer();

    @Override
    default String getCacheKey() {
        return getPlayer().toString();
    }

}
