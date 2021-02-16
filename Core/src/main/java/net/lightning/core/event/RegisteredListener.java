package net.lightning.core.event;

import lombok.Getter;

import java.lang.reflect.Method;

@Getter
class RegisteredListener {

    private final GameListener instance;
    private final Method method;
    private final GameEventHandler eventHandler;

    public RegisteredListener(GameListener instance, Method method, GameEventHandler eventHandler) {
        this.instance = instance;
        this.method = method;
        this.eventHandler = eventHandler;
    }

    public int getPriority() {
        return eventHandler.priority().ordinal();
    }

}
