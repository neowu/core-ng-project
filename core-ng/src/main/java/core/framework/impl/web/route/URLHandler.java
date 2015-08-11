package core.framework.impl.web.route;

import core.framework.api.http.HTTPMethod;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.api.web.exception.MethodNotAllowedException;
import core.framework.impl.web.ControllerProxy;

import java.util.Map;

/**
 * @author neo
 */
class URLHandler {
    final String pathPattern;
    private final Map<HTTPMethod, ControllerProxy> controllers = Maps.newHashMap();

    URLHandler(String pathPattern) {
        this.pathPattern = pathPattern;
    }

    void put(HTTPMethod method, ControllerProxy controller) {
        ControllerProxy previous = controllers.putIfAbsent(method, controller);
        if (previous != null) {
            throw Exceptions.error("conflicted controller found, path={}, method={}", pathPattern, method);
        }
    }

    ControllerProxy get(HTTPMethod method) {
        ControllerProxy proxy = controllers.get(method);
        if (proxy == null) throw new MethodNotAllowedException(method);
        return proxy;
    }
}
