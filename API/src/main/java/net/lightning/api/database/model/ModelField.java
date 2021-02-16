package net.lightning.api.database.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModelField {

    String fieldName() default "";

    String type() default "auto";

    int length() default -1;

    boolean unique() default false;

    boolean primaryKey() default false;

    boolean autoIncrement() default false;

    boolean nullable() default false;

}
