package core.framework.internal.web.service;

import core.framework.api.web.service.PathParam;
import core.framework.internal.reflect.Methods;
import core.framework.internal.reflect.Params;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static core.framework.util.Strings.format;

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
            throw new Error(format("service must impl service interface, serviceInterface={}, instance={}", serviceInterface.getCanonicalName(), service));

        Class<?> serviceClass = service.getClass();

        if (!Object.class.equals(serviceClass.getSuperclass())) {
            throw new Error("service impl class must not have super class, class=" + serviceClass.getCanonicalName());
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
                throw new Error("service impl must not have @PathParam, method=" + Methods.path(method));
            }
        }
    }
}
