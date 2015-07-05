package core.framework.impl.web.service;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Sets;
import core.framework.api.web.service.DELETE;
import core.framework.api.web.service.GET;
import core.framework.api.web.service.POST;
import core.framework.api.web.service.PUT;
import core.framework.api.web.service.Path;
import core.framework.api.web.service.PathParam;
import core.framework.impl.web.BeanValidator;

import javax.xml.bind.annotation.XmlAccessorType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

/**
 * @author neo
 */
public class ServiceInterfaceValidator {
    private final Class<?> serviceInterface;
    private final BeanValidator validator;

    public ServiceInterfaceValidator(Class<?> serviceInterface, BeanValidator validator) {
        this.serviceInterface = serviceInterface;
        this.validator = validator;
    }

    public void validate() {
        if (!serviceInterface.isInterface())
            throw Exceptions.error("service interface must be interface, serviceInterface={}", serviceInterface);

        for (Method method : serviceInterface.getDeclaredMethods()) {
            validate(method);
        }
    }

    private void validate(Method method) {
        validateHTTPMethod(method);
        Path path = method.getDeclaredAnnotation(Path.class);
        if (path == null)
            throw Exceptions.error("method must have @Path, method={}", method);

        validateReturnType(method.getGenericReturnType());

        Set<String> pathVariables = pathVariables(path.value());
        Type requestBeanType = null;

        Annotation[][] annotations = method.getParameterAnnotations();
        Type[] parameterTypes = method.getGenericParameterTypes();
        Class<?>[] parameterClasses = method.getParameterTypes();
        Set<String> pathParams = Sets.newHashSet();

        for (int i = 0; i < parameterTypes.length; i++) {
            Type paramType = parameterTypes[i];
            PathParam pathParam = pathParam(annotations[i]);
            if (pathParam != null) {
                validatePathParamType(paramType);
                pathParams.add(pathParam.value());
            } else {
                if (requestBeanType != null)
                    throw Exceptions.error("service method must not have more than one bean param, previous={}, current={}", requestBeanType, paramType);

                Class<?> paramClass = parameterClasses[i];

                if (!paramClass.equals(List.class) && !paramClass.isAnnotationPresent(XmlAccessorType.class)) {
                    throw Exceptions.error("bean param must have @XmlAccessorType, use @PathParam if it is path param, class={}", paramType);
                }
                requestBeanType = paramType;
                validator.register(paramType);
            }
        }

        if (pathVariables.size() != pathParams.size() || !pathVariables.containsAll(pathParams))
            throw Exceptions.error("service method @PathParam params must match variable in path pattern, path={}, method={}", path.value(), method);
    }

    private Set<String> pathVariables(String path) {
        Set<String> names = Sets.newHashSet();
        String[] tokens = path.split("/");
        for (String token : tokens) {
            if (token.startsWith(":")) {
                int paramIndex = token.indexOf('(');
                int endIndex = paramIndex > 0 ? paramIndex : token.length();
                boolean notDuplicated = names.add(token.substring(1, endIndex));
                if (!notDuplicated) throw Exceptions.error("path must not have duplicated param name, path={}", path);
            }
        }
        return names;
    }

    private PathParam pathParam(Annotation[] paramAnnotations) {
        for (Annotation paramAnnotation : paramAnnotations) {
            if (paramAnnotation instanceof PathParam) return (PathParam) paramAnnotation;
        }
        return null;
    }

    private void validatePathParamType(Type paramType) {
        if (!(paramType instanceof Class))
            throw Exceptions.error("path param must be class, type={}", paramType.getTypeName());

        Class<?> paramClass = (Class<?>) paramType;

        if (paramClass.isPrimitive())
            throw Exceptions.error("primitive class is not supported, please use object class, paramClass={}", paramClass);

        if (Integer.class.equals(paramClass)) return;
        if (Long.class.equals(paramClass)) return;
        if (String.class.equals(paramClass)) return;
        throw Exceptions.error("path param class is not supported, paramClass={}", paramClass);
    }

    private void validateReturnType(Type returnType) {
        if (void.class == returnType) return;
        validator.register(returnType);
    }

    private void validateHTTPMethod(Method method) {
        if (method.isAnnotationPresent(GET.class)) return;
        if (method.isAnnotationPresent(POST.class)) return;
        if (method.isAnnotationPresent(PUT.class)) return;
        if (method.isAnnotationPresent(DELETE.class)) return;
        throw Exceptions.error("method must have http method annotation, method={}", method);
    }
}
