package core.framework.api.validate;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * used for List<T> and Map<String, T> to check value are not null
 *
 * @author neo
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface ValueNotNull {
    String message() default "value must not be null";
}
