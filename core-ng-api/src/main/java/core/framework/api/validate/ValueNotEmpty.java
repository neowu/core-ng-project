package core.framework.api.validate;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * used for List<T> and Map<String, T> to check values are not empty
 *
 * @author neo
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface ValueNotEmpty {
    String message() default "value must not be empty";
}
