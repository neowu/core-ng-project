package core.framework.impl.web.route;

import core.framework.api.http.HTTPMethod;
import core.framework.api.util.Exceptions;
import core.framework.api.web.exception.MethodNotAllowedException;
import core.framework.impl.web.ControllerHolder;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author neo
 */
class URLHandler {
    final String pathPattern;
    private final Map<HTTPMethod, ControllerHolder> controllers = new EnumMap<>(HTTPMethod.class);

    URLHandler(String pathPattern) {
        this.pathPattern = pathPattern;
    }

    void put(HTTPMethod method, ControllerHolder controller) {
        ControllerHolder previous = controllers.putIfAbsent(method, controller);
        if (previous != null) {
            throw Exceptions.error("found duplicate controller, path={}, method={}", pathPattern, method);
        }
    }

    ControllerHolder get(HTTPMethod method) {
        ControllerHolder controller = controllers.get(method);
        if (controller == null) {
            throw new MethodNotAllowedException("method not allowed, method=" + method);
        }
        return controller;
    }
}
