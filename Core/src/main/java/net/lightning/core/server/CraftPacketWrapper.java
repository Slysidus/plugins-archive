package net.lightning.core.server;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@Getter
public class CraftPacketWrapper implements PacketWrapper {

    @Getter(AccessLevel.NONE)
    private final CraftServerHandler craftServerHandler;
    private final Class<?> packetClass;

    private final Object instance;

    public CraftPacketWrapper(CraftServerHandler craftServerHandler, String packetClassName) throws CraftOperationException {
        this.craftServerHandler = craftServerHandler;

        try {
            this.packetClass = craftServerHandler.getNMSClass(packetClassName);
            this.instance = packetClass.newInstance();
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            throw new CraftOperationException(ex);
        }
    }

    public CraftPacketWrapper(CraftServerHandler craftServerHandler, String packetClassName, List<Class<?>> parameters, Object... objects) throws CraftOperationException {
        this.craftServerHandler = craftServerHandler;

        try {
            this.packetClass = craftServerHandler.getNMSClass(packetClassName);
            this.instance = craftServerHandler.instantiateNMSClass(packetClassName, parameters, objects);
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
            throw new CraftOperationException(ex);
        }
    }

    public CraftPacketWrapper setValue(String fieldName, Object value) {
        try {
            craftServerHandler.setFieldValue(instance, fieldName, value);
        }
        catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new CraftOperationException(ex);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public CraftPacketWrapper addToList(String fieldName, Object value) {
        try {
            Object listObject = craftServerHandler.getFieldValue(instance, fieldName);
            Preconditions.checkArgument(List.class.isAssignableFrom(listObject.getClass()));
            ((List<Object>) listObject).add(value);
            craftServerHandler.setFieldValue(instance, fieldName, listObject);
        }
        catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new CraftOperationException(ex);
        }
        return this;
    }

    public void send(Player... receivers) {
        if (receivers.length == 0) {
            craftServerHandler.sendGlobalPacket(instance);
        }
        else {
            for (Player receiver : receivers) {
                craftServerHandler.sendPacket(receiver, instance);
            }
        }
    }

}
