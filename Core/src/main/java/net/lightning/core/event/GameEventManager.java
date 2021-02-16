package net.lightning.core.event;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.lightning.api.util.ReflectionUtil;
import net.lightning.core.LightningGamePlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class GameEventManager {

    private final LightningGamePlugin<?> plugin;

    private final List<GameListener> listeners = Lists.newArrayList();
    private Map<Class<? extends GameEvent>, LinkedList<RegisteredListener>> sorted = Maps.newHashMap();

    public GameEventManager(LightningGamePlugin<?> plugin) {
        this.plugin = plugin;
    }

    public void register(GameListener... gameListeners) {
        listeners.addAll(Arrays.asList(gameListeners));
        reload();
    }

    public void registerNative(Listener... nativeListeners) {
        PluginManager pluginManager = Bukkit.getPluginManager();
        for (Listener nativeListener : nativeListeners) {
            pluginManager.registerEvents(nativeListener, plugin);
        }
    }

    public void unregister(GameListener... gameListeners) {
        listeners.removeAll(Arrays.asList(gameListeners));
        reload();
    }

    public void unregisterNative(Listener... nativeListeners) {
        for (Listener nativeListener : nativeListeners) {
            HandlerList.unregisterAll(nativeListener);
        }
    }

    private void reload() {
        Map<Class<? extends GameEvent>, List<RegisteredListener>> unsorted = Maps.newHashMap();
        for (GameListener listener : listeners) {
            for (Method method : ReflectionUtil.getMethodsAnnotatedWith(listener.getClass(), GameEventHandler.class)) {
                if (method.getParameterCount() != 1 || !GameEvent.class.isAssignableFrom(method.getParameterTypes()[0]))
                    continue;

                @SuppressWarnings("unchecked")
                Class<? extends GameEvent> watchedEvent = (Class<? extends GameEvent>) method.getParameterTypes()[0];

                List<RegisteredListener> eventMethods = unsorted.getOrDefault(watchedEvent, Lists.newArrayList());
                eventMethods.add(new RegisteredListener(listener, method, method.getAnnotation(GameEventHandler.class)));
                unsorted.put(watchedEvent, eventMethods);
            }
        }

        Map<Class<? extends GameEvent>, LinkedList<RegisteredListener>> sorted = Maps.newHashMap();
        for (Class<? extends GameEvent> event : unsorted.keySet()) {
            sorted.put(event, unsorted.get(event).stream()
                    .sorted(Comparator.comparingInt(RegisteredListener::getPriority))
                    .collect(Collectors.toCollection(LinkedList::new)));
        }
        this.sorted = sorted;
    }

    public void fireEvent(GameEvent event) {
        Class<? extends GameEvent> eventClass = event.getClass();
        if (sorted.containsKey(eventClass)) {
            for (RegisteredListener listener : sorted.get(eventClass)) {
                if (event instanceof CancellableEvent) {
                    if (((CancellableEvent) event).isCancelled() && listener.getEventHandler().ignoreCancelled())
                        continue;
                }

                try {
                    listener.getMethod().invoke(listener.getInstance(), event);
                }
                catch (InvocationTargetException ex) {
                    ex.getTargetException().printStackTrace();
                }
                catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

}
