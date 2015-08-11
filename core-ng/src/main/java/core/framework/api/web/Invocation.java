package core.framework.api.web;

import java.lang.annotation.Annotation;

/**
 * @author neo
 */
public interface Invocation {
    <T extends Annotation> T annotation(Class<T> annotationClass);

    WebContext context();

    Response proceed() throws Exception;
}
