package core.framework.api.web.service;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author neo
 */
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface PathParam {
    String value();
}
