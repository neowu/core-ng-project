package core.framework.impl.web.route;

import core.framework.api.http.HTTPMethod;
import core.framework.api.util.Maps;
import core.framework.api.web.Controller;
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

    void put(HTTPMethod method, Controller controller) {
        String action = "web/" + method.name().toLowerCase() + "-" + actionInfo(pathPattern);
        ControllerProxy previous = controllers.putIfAbsent(method, new ControllerProxy(action, controller));
        if (previous != null) {
            throw new Error("conflicted controller found, path=" + pathPattern + ", method=" + method);
        }
    }

    ControllerProxy get(HTTPMethod method) {
        ControllerProxy proxy = controllers.get(method);
        if (proxy == null) throw new MethodNotAllowedException(method);
        return proxy;
    }

    String actionInfo(String pathPattern) {
        if ("/".equals(pathPattern)) return "root";

        String[] tokens = pathPattern.split("/");
        StringBuilder builder = new StringBuilder(pathPattern.length());
        int index = 0;
        for (String token : tokens) {
            if (token.length() == 0) continue;
            if (index > 0) builder.append('-');
            if (token.startsWith(":")) {
                int paramIndex = token.indexOf('(');
                int endIndex = paramIndex > 0 ? paramIndex : token.length();
                builder.append(token.substring(1, endIndex));
            } else {
                builder.append(token);
            }
            index++;
        }
        return builder.toString();
    }
}
