package net.lightning.core.map;

import lombok.experimental.SuperBuilder;
import net.lightning.core.loader.ConfigurationModel;
import net.lightning.core.map.annotations.FieldNotNull;
import net.lightning.core.world.NaiveRegion;
import org.jetbrains.annotations.NotNull;

/**
 * Extend this class to create a custom map model for the game mode.
 * It can be used to set per-map settings, team locations, etc.
 * Used with {@link GameMapLoader}.
 * <br>
 * <em>RULES:</em>
 * <ul>
 *     <li>
 *             Each field should be final as the map file should NOT be edited while the game is in progress.
 *             If we ever need to edit one of these values, then copy it to the game object on postInit.
 *     </li>
 *     <li>No getters/setters as fields are loaded once from config file.</li>
 *     <li>Use {@link FieldNotNull} instead of {@link NotNull}.</li>
 *     <li>Use {@link SuperBuilder} to generate constructor.</li>
 * </ul>
 */

@SuperBuilder
public class GameMapModel extends ConfigurationModel {

    /**
     * The map name.
     */
    public final @FieldNotNull String name;

    /**
     * Max player count. Can be used for both join-and-play and wait-to-start games.
     */
    public final short maxPlayers;

    /**
     * Rect bounds of the map.
     */
    public final NaiveRegion bounds;

}
