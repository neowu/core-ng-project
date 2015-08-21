package core.framework.impl.web.route;

import core.framework.api.http.HTTPMethod;
import core.framework.api.util.Maps;
import core.framework.api.web.exception.NotFoundException;
import core.framework.impl.log.ActionLog;
import core.framework.impl.web.ControllerHolder;
import core.framework.impl.web.PathParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author neo
 */
public final class Route {
    private final Logger logger = LoggerFactory.getLogger(Route.class);
    private final PathPatternValidator validator = new PathPatternValidator();

    private final Map<String, URLHandler> staticHandlers = Maps.newHashMap();
    private final PathNode dynamicRoot = new PathNode();

    public void add(HTTPMethod method, String path, ControllerHolder controller) {
        logger.info("route, {} {}", method, path);
        validator.validate(path);
        controller.action = new ActionInfo(method, path).action();

        URLHandler handler;
        if (path.contains("/:")) {
            handler = dynamicRoot.register(path);
        } else {
            handler = staticHandlers.get(path);
            if (handler == null) {
                handler = new URLHandler(path);
                staticHandlers.put(path, handler);
            }
        }
        handler.put(method, controller);
    }

    public ControllerHolder get(String path, HTTPMethod method, PathParams pathParams, ActionLog actionLog) {
        URLHandler handler = staticHandlers.get(path);
        if (handler == null) handler = dynamicRoot.find(path, pathParams);
        if (handler == null) {
            actionLog.action("web/not-found");
            throw new NotFoundException("not found, path=" + path);
        }
        actionLog.context("pathPattern", handler.pathPattern);
        return handler.get(method, actionLog);
    }
}
