package core.framework.api.validate;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Size is used on String, List and Map.
 *
 * @author neo
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface Size {
    int min() default -1;

    int max() default -1;

    String message() default "size must be between {min} and {max}, size={value}";
}
