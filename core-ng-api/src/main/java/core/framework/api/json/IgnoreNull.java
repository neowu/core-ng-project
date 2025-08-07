package core.framework.api.json;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author kent.kuan
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface IgnoreNull {
}
