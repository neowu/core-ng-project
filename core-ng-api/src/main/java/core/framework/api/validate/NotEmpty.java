package core.framework.api.validate;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * used for String field, to check string is not empty, be aware of null is not treated as empty,
 * this is to validate if the value is presented, it must not be empty string
 *
 * @author neo
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface NotEmpty {
    String message() default "field must not be empty";
}
