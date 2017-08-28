package core.framework.api.web.service;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author neo
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface QueryParam {
    String name();
}
