package net.lightning.common.models.stats;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.lightning.api.database.model.Model;
import net.lightning.api.database.model.ModelField;

import java.util.UUID;

@Data
@Model(tableName = "stats__global")
@NoArgsConstructor
public class GlobalStats implements INetworkStats {

    @Setter(AccessLevel.NONE)
    @ModelField(unique = true, primaryKey = true)
    private UUID player;

    @ModelField
    private int connections;
    @ModelField
    private long playtime;

}
