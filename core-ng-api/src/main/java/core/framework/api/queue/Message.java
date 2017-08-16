package core.framework.api.queue;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author neo
 */
@Deprecated // with kafka, it doesn't need this annotation anymore
@Target(TYPE)
@Retention(RUNTIME)
public @interface Message {
    String name();
}
