package core.framework.api.web;

import java.lang.annotation.Annotation;

/**
 * @author neo
 */
public interface Invocation {
    <T extends Annotation> T controllerAnnotation(Class<T> annotationClass);

    Request request();

    void putContext(String key, Object value);

    Response proceed() throws Exception;
}
