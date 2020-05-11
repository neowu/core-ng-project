package core.framework.api.validate;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Min can be used on numeric fields.
 *
 * @author neo
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface Max {
    double value();

    String message() default "field must not be greater than {max}, value={value}";
}
