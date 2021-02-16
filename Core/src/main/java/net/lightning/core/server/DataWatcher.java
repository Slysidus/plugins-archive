package net.lightning.core.server;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@RequiredArgsConstructor
public class DataWatcher {

    private static Method setMethod, getMethod;

    private static Class<?> dataWatcherObjectClass;
    private static Method dataWatcherGetRawMethod, dataWatcherObjectSetMethod;

    @Getter
    private final Object craftInstance;

    public void set(int index, byte value) throws CraftOperationException {
        setRaw(index, value);
    }

    public void set(int index, short value) throws CraftOperationException {
        setRaw(index, value);
    }

    public void set(int index, int value) throws CraftOperationException {
        setRaw(index, value);
    }

    public void set(int index, float value) throws CraftOperationException {
        setRaw(index, value);
    }

    private void setRaw(int index, Object object) throws CraftOperationException {
        try {
            if (setMethod == null) {
                setMethod = craftInstance.getClass().getMethod("a", int.class, Object.class);
            }

            if (dataWatcherGetRawMethod == null) {
                dataWatcherGetRawMethod = craftInstance.getClass().getDeclaredMethod("j", int.class);
            }
            if (dataWatcherObjectClass == null) {
                dataWatcherObjectClass = Class.forName(craftInstance.getClass().getTypeName() + "$WatchableObject");
            }
            if (dataWatcherObjectSetMethod == null) {
                dataWatcherObjectSetMethod = dataWatcherObjectClass.getMethod("a", Object.class);
            }

            if (!dataWatcherGetRawMethod.isAccessible()) {
                dataWatcherGetRawMethod.setAccessible(true);
            }
            Object watchableObject = dataWatcherGetRawMethod.invoke(craftInstance, index);
            if (watchableObject != null) {
                dataWatcherObjectSetMethod.invoke(dataWatcherObjectClass.cast(watchableObject), object);
            }
            else {
                if (!setMethod.isAccessible()) {
                    setMethod.setAccessible(true);
                }
                setMethod.invoke(craftInstance, index, object);
            }
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException ex) {
            throw new CraftOperationException(ex);
        }
    }

    public byte getByte(int index) {
        return (byte) getRaw(index, "getByte");
    }

    public short getShort(int index) {
        return (short) getRaw(index, "getShort");
    }

    public int getInt(int index) {
        return (int) getRaw(index, "getInt");
    }

    public float getFloat(int index) {
        return (float) getRaw(index, "getFloat");
    }

    private Object getRaw(int index, String methodName) throws CraftOperationException {
        try {
            if (getMethod == null) {
                getMethod = craftInstance.getClass().getMethod(methodName, int.class);
            }

            if (!getMethod.isAccessible()) {
                getMethod.setAccessible(true);
            }
            return getMethod.invoke(craftInstance, index);
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new CraftOperationException(ex);
        }
    }

}
