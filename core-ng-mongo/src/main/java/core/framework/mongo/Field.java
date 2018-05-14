package core.framework.mongo;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author neo
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface Field {
    String name();
}
