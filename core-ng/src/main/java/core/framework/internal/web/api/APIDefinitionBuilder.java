package core.framework.internal.web.api;

import core.framework.api.web.service.Path;
import core.framework.api.web.service.PathParam;
import core.framework.internal.log.LogManager;
import core.framework.internal.reflect.GenericTypes;
import core.framework.internal.reflect.Params;
import core.framework.internal.web.service.HTTPMethods;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author neo
 */
public class APIDefinitionBuilder {
    private final Set<Class<?>> serviceInterfaces;
    private final Set<Class<?>> beanClasses;
    private final APITypeParser parser = new APITypeParser();

    public APIDefinitionBuilder(Set<Class<?>> serviceInterfaces, Set<Class<?>> beanClasses) {
        this.serviceInterfaces = serviceInterfaces;
        this.beanClasses = beanClasses;
    }

    public APIDefinitionResponse build() {
        var response = new APIDefinitionResponse();
        response.app = LogManager.APP_NAME;
        response.version = UUID.randomUUID().toString();
        response.services = services();
        for (Class<?> beanClass : beanClasses) {
            if (beanClass.isEnum()) {
                parser.parseEnumType(beanClass);
            } else {
                parser.parseBeanType(beanClass);
            }
        }
        response.types = parser.types();
        return response;
    }

    private List<APIDefinitionResponse.Service> services() {
        var services = new ArrayList<APIDefinitionResponse.Service>(serviceInterfaces.size());
        for (Class<?> serviceInterface : serviceInterfaces) {
            services.add(service(serviceInterface));
        }
        return services;
    }

    private APIDefinitionResponse.Service service(Class<?> serviceInterface) {
        var service = new APIDefinitionResponse.Service();
        service.name = serviceInterface.getSimpleName();
        Method[] methods = serviceInterface.getMethods();
        Arrays.sort(methods, Comparator.comparing((Method method) -> method.getDeclaredAnnotation(Path.class).value()).thenComparing(method -> HTTPMethods.httpMethod(method).ordinal()));
        service.operations = new ArrayList<>(methods.length);
        for (Method method : methods) {
            var operation = new APIDefinitionResponse.Operation();
            operation.name = method.getName();
            operation.method = String.valueOf(HTTPMethods.httpMethod(method));
            operation.path = method.getDeclaredAnnotation(Path.class).value();
            parseParams(operation, method);
            operation.optional = GenericTypes.isOptional(method.getGenericReturnType());
            operation.responseType = parseResponseType(method.getGenericReturnType(), operation.optional);
            operation.deprecated = method.isAnnotationPresent(Deprecated.class);
            service.operations.add(operation);
        }
        return service;
    }

    private String parseResponseType(Type returnType, boolean optional) {
        // response can only be Optional<T> or Class
        if (optional) {
            return parser.parseType(GenericTypes.optionalValueClass(returnType)).type;
        }
        return parser.parseType(returnType).type;
    }

    private void parseParams(APIDefinitionResponse.Operation operation, Method method) {
        Annotation[][] annotations = method.getParameterAnnotations();
        Type[] paramTypes = method.getGenericParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            var type = parser.parseType(paramTypes[i]);
            PathParam pathParam = Params.annotation(annotations[i], PathParam.class);
            if (pathParam != null) {
                var param = new APIDefinitionResponse.PathParam();
                param.name = pathParam.value();
                param.type = type.type; // path param type must be simple type
                operation.pathParams.add(param);
            } else {
                operation.requestType = type.type;
            }
        }
    }
}
