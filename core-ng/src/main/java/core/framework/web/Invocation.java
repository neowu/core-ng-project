package core.framework.web;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

/**
 * @author neo
 */
public interface Invocation {
    @Nullable
    <T extends Annotation> T annotation(Class<T> annotationClass);

    WebContext context();

    Response proceed() throws Exception;
}
