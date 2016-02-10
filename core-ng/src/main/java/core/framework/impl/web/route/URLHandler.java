package core.framework.impl.web.route;

import core.framework.api.http.HTTPMethod;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.api.web.exception.MethodNotAllowedException;
import core.framework.impl.web.ControllerHolder;

import java.util.Map;

/**
 * @author neo
 */
class URLHandler {
    final String pathPattern;
    private final Map<HTTPMethod, ControllerHolder> controllers = Maps.newHashMap();

    URLHandler(String pathPattern) {
        this.pathPattern = pathPattern;
    }

    void put(HTTPMethod method, ControllerHolder controller) {
        ControllerHolder previous = controllers.putIfAbsent(method, controller);
        if (previous != null) {
            throw Exceptions.error("conflicted controller found, path={}, method={}", pathPattern, method);
        }
    }

    ControllerHolder get(HTTPMethod method) {
        ControllerHolder controller = controllers.get(method);
        if (controller == null) {
            throw new MethodNotAllowedException(method);
        }
        return controller;
    }
}
