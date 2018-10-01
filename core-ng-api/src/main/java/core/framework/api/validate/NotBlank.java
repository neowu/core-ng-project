package core.framework.api.validate;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * used for String field, to check string is not blank, be aware of null is not treated as blank,
 * this is to validate if the value presents, it must not be blank string
 *
 * @author neo
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface NotBlank {
    String message() default "field must not be blank";
}
