package core.framework.web;

import org.jspecify.annotations.Nullable;

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
