package core.framework.internal.web.route;

import core.framework.api.http.HTTPStatus;
import core.framework.http.HTTPMethod;
import core.framework.internal.log.ActionLog;
import core.framework.internal.web.controller.ControllerHolder;
import core.framework.internal.web.request.PathParams;
import core.framework.util.Maps;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public final class Route {
    private final Logger logger = LoggerFactory.getLogger(Route.class);

    private final Map<String, URLHandler> staticHandlers = Maps.newHashMap();
    private final PathNode dynamicRoot = new PathNode(null);

    public void add(HTTPMethod method, String path, ControllerHolder controller) {
        logger.info("route, method={}, path={}, controller={}", method, path, controller.controllerInfo);

        URLHandler handler;
        if (path.contains("/:")) {
            handler = dynamicRoot.register(path);
        } else {
            handler = staticHandlers.computeIfAbsent(path, URLHandler::new);
        }
        handler.put(method, controller);
    }

    // return null if handler not found / 404
    public RouteResult get(String path, HTTPMethod method, PathParams pathParams, ActionLog actionLog) {
        URLHandler handler = staticHandlers.get(path);
        if (handler == null) handler = dynamicRoot.find(path, pathParams);
        if (handler == null) {
            return new RouteResult(null, HTTPStatus.NOT_FOUND);
        }
        actionLog.context.put("path_pattern", List.of(handler.pathPattern));
        logger.debug("pathPattern={}", handler.pathPattern);
        return handler.get(method);
    }

    public record RouteResult(@Nullable ControllerHolder controller, @Nullable HTTPStatus errorStatus) {
    }
}
