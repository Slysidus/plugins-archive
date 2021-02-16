package net.lightning.core.map.annotations;

import net.lightning.core.map.GameMapLoader;
import net.lightning.core.map.GameMapModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use instead of {@link NotNull} in {@link GameMapModel}.
 * It allows the {@link GameMapLoader} to throw an error if the value is null.
 * You may need to configure your IDE for it to understand.
 * (e.g: for Intellij, {@see https://www.jetbrains.com/help/idea/nullable-notnull-configuration.html})
 * <br>
 * The annotation {@link Nullable} does NOT need a custom version as the map loader
 * can't do anything useful of this information.
 */

@NotNull
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FieldNotNull {

}
