package net.lightning.capture.game;

import com.google.common.collect.Maps;
import lombok.experimental.SuperBuilder;
import net.lightning.core.loader.ConfigurationModel;
import net.lightning.core.loader.LoaderField;
import net.lightning.core.loader.types.KeyedList;
import net.lightning.core.loader.types.ReadableBlock;
import net.lightning.core.loader.types.ReadableLocation;
import net.lightning.core.map.GameMapModel;
import net.lightning.core.map.annotations.FieldNotNull;
import net.lightning.core.util.WorldUtil;
import net.lightning.core.world.NaiveCuboid;
import org.bukkit.Location;

import java.util.Map;

@SuperBuilder
public class CaptureGameMapModel extends GameMapModel {

    public final Location respawnLocation;

    public final short killerLimit, heightLimit;

    public final KeyedList<NaiveCuboid> protectedCuboids;

    public final short sceneRadius, sceneHeight;

    @FieldNotNull
    public final TeamGroup teams;

    @SuperBuilder
    public static class TeamGroup extends ConfigurationModel {

        @FieldNotNull
        public final TeamModel red, blue;

    }

    @SuperBuilder
    public static class TeamModel extends ConfigurationModel {

        @FieldNotNull
        public final ReadableLocation spawnLocation;

        @FieldNotNull
        public final ReadableBlock spawnToRoof, roofToSpawn;

        @FieldNotNull
        public final ReadableBlock roofToRoom, roomToRoof;

        @FieldNotNull
        public final ReadableLocation kitsNpc;

        /**
         * Block that enemy team should break to steal energy.
         */
        @FieldNotNull
        public final ReadableBlock accumulator;

        /**
         * Buffer storage blocks.
         */
        @LoaderField(fixedLength = 9)
        public final KeyedList<ReadableBlock> energyStorage;

        public final KeyedList<ReadableLocation> strikesLocations;

        public final ReadableLocation endSceneStart;

        public Map.Entry<Location, CaptureRoom> getMatchingTeleporter(Location naiveLocation) {
            if (WorldUtil.naiveEqualsCheck(naiveLocation, spawnToRoof)) {
                return Maps.immutableEntry(roofToSpawn, CaptureRoom.ROOF);
            }
            else if (WorldUtil.naiveEqualsCheck(naiveLocation, roofToSpawn)) {
                return Maps.immutableEntry(spawnToRoof, CaptureRoom.OUTSIDE);
            }
            else if (WorldUtil.naiveEqualsCheck(naiveLocation, roofToRoom)) {
                return Maps.immutableEntry(roomToRoof, CaptureRoom.CONTROL_ROOM);
            }
            else if (WorldUtil.naiveEqualsCheck(naiveLocation, roomToRoof)) {
                return Maps.immutableEntry(roofToRoom, CaptureRoom.ROOF);
            }
            return null;
        }

    }

}
