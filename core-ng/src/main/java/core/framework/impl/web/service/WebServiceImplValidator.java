package core.framework.impl.web.service;

import core.framework.api.util.Exceptions;
import core.framework.api.web.service.PathParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author neo
 */
public class WebServiceImplValidator<T> {
    private final Class<T> serviceInterface;
    private final T service;

    public WebServiceImplValidator(Class<T> serviceInterface, T service) {
        this.serviceInterface = serviceInterface;
        this.service = service;
    }

    public void validate() {
        if (!serviceInterface.isInstance(service))
            throw Exceptions.error("service must impl service interface, serviceInterface={}", serviceInterface.getCanonicalName(), service);

        Class<?> serviceClass = service.getClass();
        for (Method method : serviceInterface.getDeclaredMethods()) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            try {
                Method serviceMethod = serviceClass.getMethod(method.getName(), parameterTypes);
                Annotation[][] parameterAnnotations = serviceMethod.getParameterAnnotations();
                for (Annotation[] parameterAnnotation : parameterAnnotations) {
                    for (Annotation annotation : parameterAnnotation) {
                        if (PathParam.class.equals(annotation.annotationType())) {
                            throw Exceptions.error("service impl must not have @PathParam, method={}", serviceMethod);
                        }
                    }
                }
            } catch (NoSuchMethodException e) {
                throw new Error("failed to find impl method", e);
            }
        }
    }
}
