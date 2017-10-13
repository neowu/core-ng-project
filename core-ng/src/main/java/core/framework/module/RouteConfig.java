package core.framework.module;

import core.framework.http.HTTPMethod;
import core.framework.impl.module.ModuleContext;
import core.framework.web.Controller;

/**
 * @author neo
 */
public final class RouteConfig {
    private final ModuleContext context;

    public RouteConfig(ModuleContext context) {
        this.context = context;
    }

    public void get(String path, Controller controller) {
        add(HTTPMethod.GET, path, controller);
    }

    public void post(String path, Controller controller) {
        add(HTTPMethod.POST, path, controller);
    }

    public void put(String path, Controller controller) {
        add(HTTPMethod.PUT, path, controller);
    }

    public void delete(String path, Controller controller) {
        add(HTTPMethod.DELETE, path, controller);
    }

    public void add(HTTPMethod method, String path, Controller controller) {
        context.route(method, path, controller, false);
    }
}
