package core.framework.impl.web.service;

import core.framework.api.web.service.DELETE;
import core.framework.api.web.service.GET;
import core.framework.api.web.service.POST;
import core.framework.api.web.service.PUT;
import core.framework.http.HTTPMethod;
import core.framework.util.Exceptions;

import java.lang.reflect.Method;

/**
 * @author neo
 */
public class HTTPMethodHelper {
    public static HTTPMethod httpMethod(Method method) {
        if (method.isAnnotationPresent(GET.class)) return HTTPMethod.GET;
        if (method.isAnnotationPresent(POST.class)) return HTTPMethod.POST;
        if (method.isAnnotationPresent(PUT.class)) return HTTPMethod.PUT;
        if (method.isAnnotationPresent(DELETE.class)) return HTTPMethod.DELETE;
        throw Exceptions.error("unsupported method, method={}", method);
    }
}
