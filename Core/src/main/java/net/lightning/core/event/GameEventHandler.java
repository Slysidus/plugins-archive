package net.lightning.core.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GameEventHandler {

    GameEventPriority priority() default GameEventPriority.NORMAL;

    boolean ignoreCancelled() default false;

}
