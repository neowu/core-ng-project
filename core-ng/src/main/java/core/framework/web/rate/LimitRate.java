package core.framework.web.rate;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author neo
 */
@Target({TYPE, METHOD})
@Retention(RUNTIME)
public @interface LimitRate {
    String value();
}
