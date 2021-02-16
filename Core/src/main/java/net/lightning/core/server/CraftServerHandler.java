package net.lightning.core.server;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.GameProfile;
import lombok.Getter;
import lombok.SneakyThrows;
import net.lightning.core.server.channel.PacketReaderInjector;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CraftServerHandler {

    public final String VERSION;

    @Getter
    private final JavaPlugin holderPlugin;
    @Getter
    private final PacketReaderInjector packetReaderInjector;

    private final Class<?> packetClass;
    private final Class<?> craftBukkitPlayerClass;

    public CraftServerHandler(JavaPlugin holderPlugin) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName();
        version = version.substring(version.lastIndexOf(".") + 1);
        this.VERSION = version;
        this.holderPlugin = holderPlugin;

        this.packetReaderInjector = new PacketReaderInjector(this);

        this.packetClass = Class.forName("net.minecraft.server." + version + ".Packet");
        this.craftBukkitPlayerClass = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");
    }

    /*
    NMS
     */

    public void addToTablist(Player bukkitPlayer) throws CraftOperationException {
        packet_addOrRemoveEntity(getHandle(bukkitPlayer), false).send();
    }

    public void removeFromTablist(Player bukkitPlayer) throws CraftOperationException {
        packet_addOrRemoveEntity(getHandle(bukkitPlayer), true).send();
    }

    public PacketWrapper packet_addOrRemoveEntity(Object entity, boolean remove) throws CraftOperationException {
        try {
            Class<?> packetPlayOutPlayerInfo = getNMSClass("PacketPlayOutPlayerInfo");
            Object enumValue = getNMSEnumValue("PacketPlayOutPlayerInfo$EnumPlayerInfoAction", remove ? "REMOVE_PLAYER" : "ADD_PLAYER");

            Class<?> entityPlayerClass = getNMSClass("EntityPlayer");
            Object[] array = (Object[]) Array.newInstance(entityPlayerClass, 0);
            array = ArrayUtils.add(array, entityPlayerClass.cast(entity));
            Constructor<?> packetConstructor = packetPlayOutPlayerInfo.getConstructor(enumValue.getClass(), array.getClass());
            return new ExisitingPacketWrapper(this, packetConstructor.newInstance(enumValue, array));
        }
        catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException ex) {
            throw new CraftOperationException(ex);
        }
    }

    public PacketWrapper packet_addOrRemoveFakePlayer(GameProfile gameProfile, String tablistName, boolean remove) throws CraftOperationException {
        try {
            CraftPacketWrapper packetWrapper = new CraftPacketWrapper(this, "PacketPlayOutPlayerInfo")
                    .setValue("a", getNMSEnumValue("PacketPlayOutPlayerInfo$EnumPlayerInfoAction", remove ? "REMOVE_PLAYER" : "ADD_PLAYER"));

            Object enumGamemode = getNMSEnumValue("WorldSettings$EnumGamemode", "NOT_SET");
            Object tablistNameComponent = getTextChatComponent(tablistName);

            Class<?> playerInfoDataClass = getNMSClass("PacketPlayOutPlayerInfo$PlayerInfoData");
            Constructor<?> playerInfoDataConstructor = playerInfoDataClass.getConstructor(
                    packetWrapper.getPacketClass(),
                    GameProfile.class, int.class,
                    enumGamemode.getClass(),
                    getNMSClass("IChatBaseComponent"));

            packetWrapper.addToList("b",
                    playerInfoDataConstructor.newInstance(packetWrapper.getInstance(), gameProfile, 0, enumGamemode, tablistNameComponent));
            return packetWrapper;
        }
        catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException ex) {
            throw new CraftOperationException(ex);
        }
    }

    public PacketWrapper packet_sendTitle(String title, String subTitle, int fadeIn, int duration, int fadeOut) throws CraftOperationException {
        Preconditions.checkArgument(title != null || subTitle != null);
        try {
            Collection<Object> packets = new ArrayList<>();
            if (title != null) {
                packets.add(getTitlePacket(title, "TITLE"));
            }
            if (subTitle != null) {
                packets.add(getTitlePacket(subTitle, "SUBTITLE"));
            }

            if (fadeIn > 0 || duration > 0 || fadeOut > 0) {
                packets.add(new CraftPacketWrapper(this, "PacketPlayOutTitle")
                        .setValue("a", getNMSEnumValue("PacketPlayOutTitle$EnumTitleAction", "TIMES"))
                        .setValue("c", Math.max(0, fadeIn))
                        .setValue("d", Math.max(0, duration))
                        .setValue("e", Math.max(0, fadeOut))
                        .getInstance());
            }

            return new MultiplePacketWrapper(this, packets);
        }
        catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new CraftOperationException(ex);
        }
    }

    public PacketWrapper packet_titleEnum(String enumValue) throws CraftOperationException {
        try {
            return new CraftPacketWrapper(this, "PacketPlayOutTitle")
                    .setValue("a", getNMSEnumValue("PacketPlayOutTitle$EnumTitleAction", enumValue));
        }
        catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new CraftOperationException(ex);
        }
    }

    private Object getTitlePacket(String title, String type)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ChatColor color = null;
        if (title.length() > 2 && title.startsWith(String.valueOf(ChatColor.COLOR_CHAR))) {
            color = ChatColor.getByChar(title.charAt(1));
        }
        Object titleChatComponent = getChatComponent(ImmutableMap.of("text", title, "color", (color != null ? color : ChatColor.WHITE).toString().toLowerCase()));
        return new CraftPacketWrapper(this, "PacketPlayOutTitle")
                .setValue("a", getNMSEnumValue("PacketPlayOutTitle$EnumTitleAction", type))
                .setValue("b", titleChatComponent)
                .getInstance();
    }

    /*
    Craft operations
     */

    public int incrementEntityCount() throws CraftOperationException {
        try {
            Class<?> entityClass = getNMSClass("Entity");
            Field entityCountField = entityClass.getDeclaredField("entityCount");
            if (!entityCountField.isAccessible()) {
                entityCountField.setAccessible(true);
            }

            int entityCount = (int) entityCountField.get(null);
            entityCountField.set(null, entityCount + 1);
            return entityCount;
        }
        catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException ex) {
            throw new CraftOperationException(ex);
        }
    }

    public DataWatcher createDataWatcher(Object entity) throws CraftOperationException {
        try {
            return new DataWatcher(instantiateNMSClass("DataWatcher", Collections.singletonList(getNMSClass("Entity")), entity));
        }
        catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException ex) {
            throw new CraftOperationException(ex);
        }
    }

    public Object getTextChatComponent(String text) throws CraftOperationException {
        try {
            Class<?> chatSerializerClass = getNMSClass("IChatBaseComponent$ChatSerializer");
            Method convertMethod = chatSerializerClass.getMethod("a", String.class);
            return convertMethod.invoke(null, "{\"text\": \"" + text + "\"}");
        }
        catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | InvocationTargetException ex) {
            throw new CraftOperationException(ex);
        }
    }

    public Object getChatComponent(Map<String, String> values) throws CraftOperationException {
        try {
            Class<?> chatSerializerClass = getNMSClass("IChatBaseComponent$ChatSerializer");
            Method convertMethod = chatSerializerClass.getMethod("a", String.class);

            final String json = values.entrySet().stream()
                    .map(entry -> "\"" + entry.getKey() + "\": \"" + entry.getValue().replaceAll("\"", "\\\"") + "\"")
                    .collect(Collectors.joining(", "));
            return convertMethod.invoke(null, "{" + json + "}");
        }
        catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | InvocationTargetException ex) {
            throw new CraftOperationException(ex);
        }
    }

    public String getTextFromChatComponent(Object chatComponent) throws CraftOperationException {
        if (chatComponent == null) {
            return null;
        }

        try {
            Method getTextMethod = chatComponent.getClass().getMethod("getText");
            return (String) getTextMethod.invoke(chatComponent);
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new CraftOperationException(ex);
        }
    }

    /*
    Other
     */

    public PlayerNPC createNPC(World bukkitWorld, UUID uniqueId, String name) throws CraftOperationException {
        try {
            Object minecraftServer = getMinecraftServer();
            Object worldServer = getWorldServer(bukkitWorld);

            Object interactManager = instantiateNMSClass("PlayerInteractManager",
                    Collections.singletonList(getNMSClass("World")), worldServer);

            GameProfile gameProfile = new GameProfile(uniqueId, name);
            Object nmsEntity = instantiateNMSClass("EntityPlayer",
                    Arrays.asList(getNMSClass("MinecraftServer"), worldServer.getClass(), GameProfile.class, interactManager.getClass()),
                    minecraftServer, worldServer, gameProfile, interactManager);

            return new PlayerNPC(this, gameProfile, nmsEntity);
        }
        catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException | NoSuchFieldException ex) {
            throw new CraftOperationException(ex);
        }
    }

    /*
    Hacks
     */

    @SuppressWarnings("unchecked")
    public FakeJavaPlugin injectClasspath(File jarFile)
            throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException, NoSuchMethodException, ClassNotFoundException {
        JavaPluginLoader javaPluginLoader = getJavaPluginLoader();
        PluginDescriptionFile description = new PluginDescriptionFile(jarFile.getName(), "UNSUPPORTED", "net.lightning.core.server.FakeJavaPlugin");

        ClassLoader currentClassLoader = holderPlugin.getClass().getClassLoader();
        System.out.println(currentClassLoader.getClass().getCanonicalName());
        if (!currentClassLoader.getClass().getCanonicalName().equals("org.bukkit.plugin.java.PluginClassLoader")) {
            throw new IllegalStateException("Invalid class loader for holder plugin");
        }

        JavaPlugin originalPlugin = (JavaPlugin) getFieldValue(currentClassLoader, "plugin");
        if (originalPlugin != holderPlugin) {
            throw new IllegalStateException("Invalid plugin in class loader");
        }

        // These fields are theorically not queried anywhere
        setFieldValue(currentClassLoader, "plugin", null);
        setFieldValue(currentClassLoader, "pluginInit", null);

        Object pluginClassLoader = createPluginClassLoader(javaPluginLoader, description, jarFile.getParentFile(), jarFile);
        if (pluginClassLoader == currentClassLoader) {
            throw new IllegalStateException();
        }

        setFieldValue(currentClassLoader, "plugin", originalPlugin);
        setFieldValue(currentClassLoader, "pluginInit", originalPlugin);

        Map<String, Object> loaders = (Map<String, Object>) getFieldValue(javaPluginLoader, "loaders");
        loaders.put(description.getName(), pluginClassLoader);
        return (FakeJavaPlugin) getFieldValue(pluginClassLoader, "plugin");
    }

    private Object createPluginClassLoader(JavaPluginLoader javaPluginLoader, PluginDescriptionFile pluginDescriptionFile, File dataFolder, File file)
            throws ClassNotFoundException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        Class<?> pluginClassLoaderClass = Class.forName("org.bukkit.plugin.java.PluginClassLoader");
        Constructor<?> constructor = pluginClassLoaderClass.getDeclaredConstructor(
                javaPluginLoader.getClass(), ClassLoader.class, PluginDescriptionFile.class, File.class, File.class
        );
        constructor.setAccessible(true);
        return constructor.newInstance(
                javaPluginLoader,
                javaPluginLoader.getClass().getClassLoader(),
                pluginDescriptionFile,
                dataFolder,
                file);
    }

    @SuppressWarnings("unchecked")
    public JavaPluginLoader getJavaPluginLoader()
            throws NoSuchFieldException, IllegalAccessException {
        PluginManager pluginManager = Bukkit.getPluginManager();
        if (!(pluginManager instanceof SimplePluginManager)) {
            throw new IllegalStateException("Expecting a simple plugin manager but found something else.");
        }

        Map<Pattern, PluginLoader> fileAssociations = (Map<Pattern, PluginLoader>) getFieldValue(pluginManager, "fileAssociations");
        for (PluginLoader pluginLoader : fileAssociations.values()) {
            if (pluginLoader instanceof JavaPluginLoader) {
                return (JavaPluginLoader) pluginLoader;
            }
        }
        return null;
    }

    /*
    Global methods
     */

    public Object getMinecraftServer()
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> craftServerClass = getCraftClass("CraftServer");
        Method getMethod = craftServerClass.getMethod("getServer");
        return getMethod.invoke(craftServerClass.cast(Bukkit.getServer()));
    }

    public Object getWorldServer(World bukkitWorld)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> craftWorldClass = getCraftClass("CraftWorld");
        Method getMethod = craftWorldClass.getMethod("getHandle");
        return getNMSClass("WorldServer").cast(getMethod.invoke(craftWorldClass.cast(bukkitWorld)));
    }

    public Object instantiateNMSClass(String relativePath, List<Class<?>> parameters, Object... objects)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> clazz = getNMSClass(relativePath);
        Preconditions.checkNotNull(clazz);
        return clazz.getConstructor(parameters.toArray(new Class[0]))
                .newInstance(objects);
    }

    public Class<?> getNMSClass(String relativePath)
            throws ClassNotFoundException {
        Preconditions.checkNotNull(relativePath);
        return Class.forName("net.minecraft.server." + VERSION + "." + relativePath);
    }

    public Object getNMSEnumValue(String relativePath, String enumName)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> enumClass = getNMSClass(relativePath);
        Method valueOfMethod = enumClass.getMethod("valueOf", String.class);
        return valueOfMethod.invoke(null, enumName);
    }

    public Class<?> getCraftClass(String relativePath)
            throws ClassNotFoundException {
        Preconditions.checkNotNull(relativePath);
        return Class.forName("org.bukkit.craftbukkit." + VERSION + "." + relativePath);
    }

    public Object getCraftBukkitPlayer(Player bukkitPlayer) {
        return craftBukkitPlayerClass.cast(bukkitPlayer);
    }

    public GameProfile getGameProfile(Player bukkitPlayer) throws CraftOperationException {
        try {
            Object craftPlayer = getCraftBukkitPlayer(bukkitPlayer);
            Method getProfileMethod = craftPlayer.getClass().getMethod("getProfile");
            return (GameProfile) getProfileMethod.invoke(craftPlayer);
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new CraftOperationException(ex);
        }
    }

    public Object getHandle(Player bukkitPlayer) throws CraftOperationException {
        try {
            Method craftPlayerHandleMethod = craftBukkitPlayerClass.getDeclaredMethod("getHandle");
            return craftPlayerHandleMethod.invoke(getCraftBukkitPlayer(bukkitPlayer));
        }
        catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
            throw new CraftOperationException(ex);
        }
    }

    public Object getPlayerConnection(Player bukkitPlayer) {
        try {
            return getFieldValue(getHandle(bukkitPlayer), "playerConnection");
        }
        catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new CraftOperationException(ex);
        }
    }

    private void sendPacket(Object craftPlayerHandle, Object packet) throws CraftOperationException {
        try {
            Object playerConnection = getFieldValue(craftPlayerHandle, "playerConnection");
            Method sendPacketMethod = playerConnection.getClass().getDeclaredMethod("sendPacket", packetClass);
            sendPacketMethod.invoke(playerConnection, packet);
        }
        catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
            throw new CraftOperationException(ex);
        }

    }

    public void sendPacket(Player bukkitPlayer, Object packet) throws CraftOperationException {
        sendPacket(getHandle(bukkitPlayer), packet);
    }

    public void sendGlobalPacket(Object packet) throws CraftOperationException {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            sendPacket(onlinePlayer, packet);
        }
    }

    /*
    Reflection utils
     */

    public Object getFieldValue(Object instance, String fieldName)
            throws NoSuchFieldException, IllegalAccessException {
        return getFieldValue(instance.getClass(), instance, fieldName);
    }

    public Object getFieldValue(Class<?> clazz, Object instance, String fieldName)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = clazz.getDeclaredField(fieldName);
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        return field.get(instance);
    }

    public void setFieldValue(Object instance, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = instance.getClass().getDeclaredField(fieldName);
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        field.set(instance, value);
    }

}
