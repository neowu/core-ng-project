package core.framework.db;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author neo
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface PrimaryKey {
    // only allow auto increment on primary key, not regular column
    boolean autoIncrement() default false;
}
