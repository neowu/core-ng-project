package core.framework.internal.web.route;

import core.framework.api.http.HTTPStatus;
import core.framework.http.HTTPMethod;
import core.framework.internal.web.controller.ControllerHolder;
import core.framework.util.Maps;

import java.util.Map;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class URLHandler {
    final String pathPattern;
    private final Map<HTTPMethod, ControllerHolder> controllers = Maps.newEnumMap(HTTPMethod.class);

    URLHandler(String pathPattern) {
        this.pathPattern = pathPattern;
    }

    void put(HTTPMethod method, ControllerHolder controller) {
        ControllerHolder previous = controllers.putIfAbsent(method, controller);
        if (previous != null) {
            throw new Error(format("found duplicate controller, path={}, method={}", pathPattern, method));
        }
    }

    Route.RouteResult get(HTTPMethod method) {
        ControllerHolder controller = controllers.get(method);
        if (controller == null) {
            return new Route.RouteResult(null, HTTPStatus.METHOD_NOT_ALLOWED);
        }
        return new Route.RouteResult(controller, null);
    }
}
