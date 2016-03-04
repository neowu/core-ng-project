package core.framework.api.validate;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Length can be used on String, List and Map fields.
 *
 * @author neo
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface Length {
    int min() default -1;

    int max() default -1;

    String message() default "length must be between min and max";
}
