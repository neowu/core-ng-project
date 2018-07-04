package core.framework.impl.web.service;

import core.framework.api.web.service.DELETE;
import core.framework.api.web.service.GET;
import core.framework.api.web.service.PATCH;
import core.framework.api.web.service.POST;
import core.framework.api.web.service.PUT;
import core.framework.api.web.service.Path;
import core.framework.api.web.service.PathParam;
import core.framework.http.HTTPMethod;
import core.framework.impl.reflect.GenericTypes;
import core.framework.impl.reflect.Methods;
import core.framework.impl.reflect.Params;
import core.framework.impl.validate.type.JSONTypeValidator;
import core.framework.impl.web.bean.RequestBeanMapper;
import core.framework.impl.web.bean.ResponseBeanMapper;
import core.framework.impl.web.route.PathPatternValidator;
import core.framework.util.Exceptions;
import core.framework.util.Maps;
import core.framework.util.Sets;
import core.framework.util.Strings;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

/**
 * @author neo
 */
public class WebServiceInterfaceValidator {
    private final Class<?> serviceInterface;
    private final RequestBeanMapper requestBeanMapper;
    private final ResponseBeanMapper responseBeanMapper;

    public WebServiceInterfaceValidator(Class<?> serviceInterface, RequestBeanMapper requestBeanMapper, ResponseBeanMapper responseBeanMapper) {
        this.serviceInterface = serviceInterface;
        this.requestBeanMapper = requestBeanMapper;
        this.responseBeanMapper = responseBeanMapper;
    }

    public void validate() {
        if (!serviceInterface.isInterface())
            throw Exceptions.error("service interface must be interface, serviceInterface={}", serviceInterface.getCanonicalName());

        Map<String, Method> methodNames = Maps.newHashMap();
        for (Method method : serviceInterface.getDeclaredMethods()) {
            Method previous = methodNames.put(method.getName(), method);
            if (previous != null) {
                throw Exceptions.error("found duplicate method name which can be confusing, please use different method name, method={}, previous={}", Methods.path(method), Methods.path(previous));
            }
            validate(method);
        }
    }

    private void validate(Method method) {
        validateHTTPMethod(method);

        HTTPMethod httpMethod = HTTPMethods.httpMethod(method);

        Path path = method.getDeclaredAnnotation(Path.class);
        if (path == null) throw Exceptions.error("service method must have @Path, method={}", Methods.path(method));
        new PathPatternValidator(path.value()).validate();

        validateResponseBeanType(method.getGenericReturnType(), method);

        Set<String> pathVariables = pathVariables(path.value(), method);
        Class<?> requestBeanClass = null;

        Annotation[][] annotations = method.getParameterAnnotations();
        Type[] paramTypes = method.getGenericParameterTypes();
        Set<String> pathParams = Sets.newHashSet();

        for (int i = 0; i < paramTypes.length; i++) {
            Type paramType = paramTypes[i];
            PathParam pathParam = Params.annotation(annotations[i], PathParam.class);
            if (pathParam != null) {
                validatePathParamType(paramType, method);
                pathParams.add(pathParam.value());
            } else {
                if (requestBeanClass != null)
                    throw Exceptions.error("service method must not have more than one bean param, previous={}, current={}, method={}", requestBeanClass.getTypeName(), paramType.getTypeName(), Methods.path(method));
                requestBeanClass = GenericTypes.rawClass(paramType);

                validateRequestBeanClass(requestBeanClass, method);

                if (httpMethod == HTTPMethod.GET || httpMethod == HTTPMethod.DELETE) {
                    requestBeanMapper.registerQueryParamBean(requestBeanClass);
                } else {
                    requestBeanMapper.registerRequestBean(requestBeanClass);
                }
            }
        }

        if (pathVariables.size() != pathParams.size() || !pathVariables.containsAll(pathParams))
            throw Exceptions.error("service method @PathParam params must match variable in path pattern, path={}, method={}", path.value(), Methods.path(method));
    }

    void validateRequestBeanClass(Class<?> beanClass, Method method) {    // due to it's common to forget @PathParam in service method param, this is to make error message more friendly
        boolean isValueType = isValueType(beanClass);
        if (isValueType)
            throw Exceptions.error("request bean type must be bean class, if it is path param, please add @PathParam, type={}, method={}", beanClass.getCanonicalName(), Methods.path(method));
    }

    private Set<String> pathVariables(String path, Method method) {
        Set<String> names = Sets.newHashSet();
        String[] tokens = Strings.split(path, '/');
        for (String token : tokens) {
            if (Strings.startsWith(token, ':')) {
                int paramIndex = token.indexOf('(');
                int endIndex = paramIndex > 0 ? paramIndex : token.length();
                boolean isNew = names.add(token.substring(1, endIndex));
                if (!isNew) throw Exceptions.error("path must not have duplicate param name, path={}, method={}", path, Methods.path(method));
            }
        }
        return names;
    }

    private void validatePathParamType(Type paramType, Method method) {
        if (!(paramType instanceof Class))
            throw Exceptions.error("path param must be class type, type={}, method={}", paramType.getTypeName(), Methods.path(method));

        Class<?> paramClass = (Class<?>) paramType;

        if (paramClass.isPrimitive())
            throw Exceptions.error("primitive class is not supported, please use object class, paramClass={}, method={}", paramClass, Methods.path(method));

        if (Integer.class.equals(paramClass)) return;
        if (Long.class.equals(paramClass)) return;
        if (String.class.equals(paramClass)) return;
        if (paramClass.isEnum()) {
            JSONTypeValidator.validateEnum(paramClass);
            return;
        }
        throw Exceptions.error("path param class is not supported, paramClass={}, method={}", paramClass.getCanonicalName(), Methods.path(method));
    }

    void validateResponseBeanType(Type beanType, Method method) {
        if (void.class == beanType) return;

        // due to it's common to return wrong type as response, this is to make error message more friendly
        boolean isGenericButNotOptional = beanType instanceof ParameterizedType && !GenericTypes.isGenericOptional(beanType);
        boolean isValueType = beanType instanceof Class && isValueType((Class<?>) beanType);
        if (isGenericButNotOptional || isValueType)
            throw Exceptions.error("response bean type must be bean class or Optional<T>, type={}, method={}", beanType.getTypeName(), Methods.path(method));

        responseBeanMapper.register(beanType);
    }

    private void validateHTTPMethod(Method method) {
        int count = 0;
        if (method.isAnnotationPresent(GET.class)) count++;
        if (method.isAnnotationPresent(POST.class)) count++;
        if (method.isAnnotationPresent(PUT.class)) count++;
        if (method.isAnnotationPresent(DELETE.class)) count++;
        if (method.isAnnotationPresent(PATCH.class)) count++;
        if (count != 1)
            throw Exceptions.error("method must have exact one http method annotation, method={}", Methods.path(method));
    }

    private boolean isValueType(Class<?> type) {
        return type.isPrimitive() || type.getPackageName().startsWith("java") || type.isEnum();
    }
}
