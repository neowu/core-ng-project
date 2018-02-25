package core.framework.impl.web.route;

import core.framework.http.HTTPMethod;
import core.framework.impl.log.ActionLog;
import core.framework.impl.web.ControllerHolder;
import core.framework.impl.web.request.PathParams;
import core.framework.util.Maps;
import core.framework.web.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author neo
 */
public final class Route {
    private final Logger logger = LoggerFactory.getLogger(Route.class);

    private final Map<String, URLHandler> staticHandlers = Maps.newHashMap();
    private final PathNode dynamicRoot = new PathNode();

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

    public ControllerHolder get(String path, HTTPMethod method, PathParams pathParams, ActionLog actionLog) {
        URLHandler handler = staticHandlers.get(path);
        if (handler == null) handler = dynamicRoot.find(path, pathParams);
        if (handler == null) {
            throw new NotFoundException("not found, path=" + path, "PATH_NOT_FOUND");
        }
        actionLog.context("pathPattern", handler.pathPattern);
        return handler.get(method);
    }
}
