package net.lightning.core.loader;

import org.bukkit.ChatColor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoaderField {

    /**
     * Use a custom key instead of the one matching the field name.
     *
     * @return Overridden key for field.
     */
    String key() default "";

    /**
     * Converts '&' chars to actual colors. Works only with String.
     *
     * @return If {@link ChatColor#translateAlternateColorCodes(char, String)} should be applied to the string.
     */
    boolean formatColors() default false;

    /**
     * Set a fixed length to a list.
     *
     * @return The fixed length (unsigned) of this list.
     */
    int fixedLength() default -1;

//    /**
//     * Exclude the world from the location
//     *
//     * @return If {@link Location#getWorld()} should be excluded from location loading/saving.
//     */
//    boolean excludeWorld() default false;

    /**
     * Use a custom adapter for this field instead of the one from ConfigurationModelLoader.
     *
     * @return Custom adapter.
     */
    Class<? extends ConfigValueAdapter> customAdapter() default ConfigValueAdapter.class;

}
