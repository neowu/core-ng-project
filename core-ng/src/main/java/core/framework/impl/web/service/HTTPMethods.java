package core.framework.impl.web.service;

import core.framework.api.web.service.DELETE;
import core.framework.api.web.service.GET;
import core.framework.api.web.service.PATCH;
import core.framework.api.web.service.POST;
import core.framework.api.web.service.PUT;
import core.framework.http.HTTPMethod;

import java.lang.reflect.Method;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class HTTPMethods {
    public static HTTPMethod httpMethod(Method method) {
        if (method.isAnnotationPresent(GET.class)) return HTTPMethod.GET;
        if (method.isAnnotationPresent(POST.class)) return HTTPMethod.POST;
        if (method.isAnnotationPresent(PUT.class)) return HTTPMethod.PUT;
        if (method.isAnnotationPresent(DELETE.class)) return HTTPMethod.DELETE;
        if (method.isAnnotationPresent(PATCH.class)) return HTTPMethod.PATCH;
        throw new Error(format("unsupported method, method={}", method));
    }
}
