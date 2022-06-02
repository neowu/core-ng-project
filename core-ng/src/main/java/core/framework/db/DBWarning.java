package core.framework.db;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author neo
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface DBWarning {
    int maxOperations() default -1;

    int maxRows() default -1;
}
