package net.lightning.common.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.lightning.api.database.model.Model;
import net.lightning.api.database.model.ModelField;
import net.lightning.common.SanctionType;

import java.util.UUID;

@Data
@Model(tableName = "moderation__sanctions")
@AllArgsConstructor
public class Sanction {

    @ModelField
    private SanctionType type;

    @ModelField
    private UUID player, punisher;

    @ModelField
    private boolean active;

    @ModelField
    private String reason;

}
