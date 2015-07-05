package core.framework.api.module;

import core.framework.api.http.HTTPMethod;
import core.framework.api.web.Controller;

/**
 * @author neo
 */
public interface RouteConfig {
    default void get(String path, Controller controller) {
        add(HTTPMethod.GET, path, controller);
    }

    default void post(String path, Controller controller) {
        add(HTTPMethod.POST, path, controller);
    }

    default void put(String path, Controller controller) {
        add(HTTPMethod.PUT, path, controller);
    }

    default void delete(String path, Controller controller) {
        add(HTTPMethod.DELETE, path, controller);
    }

    void add(HTTPMethod method, String path, Controller controller);
}
