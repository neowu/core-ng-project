package core.framework.impl.web.service;

import core.framework.api.web.service.PathParam;
import core.framework.impl.reflect.Methods;
import core.framework.impl.reflect.Params;
import core.framework.util.Exceptions;

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

        if (!Object.class.equals(serviceClass.getSuperclass())) {
            throw Exceptions.error("service impl class must not have super class, class={}", serviceClass.getCanonicalName());
        }

        for (Method method : serviceInterface.getDeclaredMethods()) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            try {
                Method serviceMethod = serviceClass.getMethod(method.getName(), parameterTypes);
                validateMethod(serviceMethod);
            } catch (NoSuchMethodException e) {
                throw new Error("failed to find impl method", e);
            }
        }
    }

    private void validateMethod(Method method) {
        Annotation[][] annotations = method.getParameterAnnotations();
        for (Annotation[] parameterAnnotations : annotations) {
            PathParam pathParam = Params.annotation(parameterAnnotations, PathParam.class);
            if (pathParam != null) {
                throw Exceptions.error("service impl must not have @PathParam, method={}", Methods.path(method));
            }
        }
    }
}
