package net.lightning.core.server;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.lightning.core.GamePlayer;
import net.lightning.core.util.ArrayPool;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Getter
@ToString
@EqualsAndHashCode
public class PlayerNPC {

    private static boolean staticLoaded;

    private static Class<?> entityPlayerClass, entityHumanClass, entityClass;
    private static Method setPositionRotationMethod;

    @Getter(AccessLevel.NONE)
    private final CraftServerHandler craftServerHandler;

    private final Object nmsEntity;
    private final int id;

    private final GameProfile profile;
    private final Set<SkinFlag> skinFlags;
    private final DataWatcher dataWatcher;

    private final ArrayPool.ConsumerPool<GamePlayer> clickListeners;

    public PlayerNPC(CraftServerHandler craftServerHandler, GameProfile profile, Object nmsEntity)
            throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException {
        if (!staticLoaded) {
            entityPlayerClass = craftServerHandler.getNMSClass("EntityPlayer");
            entityHumanClass = craftServerHandler.getNMSClass("EntityHuman");
            entityClass = craftServerHandler.getNMSClass("Entity");
            setPositionRotationMethod = entityPlayerClass.getMethod("setPositionRotation", double.class, double.class, double.class, float.class, float.class);

            staticLoaded = true;
        }

        this.craftServerHandler = craftServerHandler;
        this.profile = profile;

        this.nmsEntity = nmsEntity;
        this.id = (int) craftServerHandler.getFieldValue(entityClass, nmsEntity, "id");
        this.dataWatcher = new DataWatcher(craftServerHandler.getFieldValue(entityClass, nmsEntity, "datawatcher"));

        this.skinFlags = new HashSet<>(Arrays.asList(
                SkinFlag.JACKET, SkinFlag.HAT,
                SkinFlag.LEFT_SLEEVE, SkinFlag.RIGHT_SLEEVE,
                SkinFlag.LEFT_PANTS_LEG, SkinFlag.RIGHT_PANTS_LEG
        ));
        updateSkinFlags();

        this.clickListeners = new ArrayPool.ConsumerPool<>();
    }

    public void addClickListener(Consumer<GamePlayer> clickListener) {
        clickListeners.add(clickListener);
    }

    public void setSkin(String texture, String signature) {
        profile.getProperties().put("textures", new Property("textures", texture, signature));
    }

    public String getName() {
        return profile.getName();
    }

    public void setListName(String listName) throws CraftOperationException {
        try {
            craftServerHandler.setFieldValue(nmsEntity, "listName", craftServerHandler.getTextChatComponent(listName));
        }
        catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new CraftOperationException(ex);
        }
    }

    public String getListName() throws CraftOperationException {
        try {
            return craftServerHandler.getTextFromChatComponent(craftServerHandler.getFieldValue(nmsEntity, "listName"));
        }
        catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new CraftOperationException(ex);
        }
    }

    private byte getSkinFlagsBitMask() {
        byte bitMask = 0;
        for (SkinFlag skinFlag : skinFlags) {
            bitMask |= skinFlag.mask;
        }
        return bitMask;
    }

    public void updateSkinFlags() {
        dataWatcher.set(10, getSkinFlagsBitMask());
    }

    public void spawn(Player... players) {
        craftServerHandler.packet_addOrRemoveEntity(nmsEntity, false)
                .send(players);
        new CraftPacketWrapper(craftServerHandler, "PacketPlayOutNamedEntitySpawn",
                Collections.singletonList(entityHumanClass), nmsEntity)
                .send(players);
        fixRotation(players);
    }

    public void hideFromTabList(Player... players) {
        craftServerHandler.packet_addOrRemoveEntity(nmsEntity, true)
                .send(players);
    }

    public void setLocation(Location location) throws CraftOperationException {
        try {
            setPositionRotationMethod.invoke(nmsEntity, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
            fixRotation();
        }
        catch (IllegalAccessException | InvocationTargetException ex) {
            throw new CraftOperationException(ex);
        }
    }

    public void fixRotation(Player... players) throws CraftOperationException {
        try {
            new CraftPacketWrapper(craftServerHandler, "PacketPlayOutEntityHeadRotation")
                    .setValue("a", id)
                    .setValue("b", (byte) (((float) craftServerHandler.getFieldValue(entityClass, nmsEntity, "yaw")) * 256 / 360))
                    .send(players);
        }
        catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new CraftOperationException(ex);
        }
    }

    public enum SkinFlag {

        CAPE(0x01),
        JACKET(0x02),
        LEFT_SLEEVE(0x04),
        RIGHT_SLEEVE(0x08),
        LEFT_PANTS_LEG(0x10),
        RIGHT_PANTS_LEG(0x20),
        HAT(0x40);

        private final byte mask;

        SkinFlag(int mask) {
            this.mask = (byte) mask;
        }

    }

}
