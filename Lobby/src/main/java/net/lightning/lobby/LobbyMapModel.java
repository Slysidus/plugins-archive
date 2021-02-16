package net.lightning.lobby;

import lombok.experimental.SuperBuilder;
import net.lightning.core.loader.types.ReadableLocation;
import net.lightning.core.map.GameMapModel;
import net.lightning.core.map.annotations.FieldNotNull;

@SuperBuilder
public class LobbyMapModel extends GameMapModel {

    public final @FieldNotNull ReadableLocation spawn;

    public final int voidLimit;

}
