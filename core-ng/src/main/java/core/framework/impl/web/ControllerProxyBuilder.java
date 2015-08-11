package core.framework.impl.web;

import core.framework.api.http.HTTPMethod;
import core.framework.api.web.Controller;

import java.lang.reflect.Method;

/**
 * @author neo
 */
public class ControllerProxyBuilder {
    private final HTTPMethod method;
    private final String pathPattern;
    private final Controller controller;

    private Method targetMethod;

    public ControllerProxyBuilder(HTTPMethod method, String pathPattern, Controller controller) {
        this.method = method;
        this.pathPattern = pathPattern;
        this.controller = controller;
    }

    public ControllerProxyBuilder targetMethod(Method targetMethod) {
        this.targetMethod = targetMethod;
        return this;
    }

    public ControllerProxy build() {
        String action = "web/" + method.name().toLowerCase() + "-" + actionInfo(pathPattern);
        String targetClassName;
        String targetMethodName;
        if (targetMethod == null) {
            ControllerInspector inspector = new ControllerInspector(controller);
            targetMethod = inspector.targetMethod;
            targetClassName = inspector.targetClassName;
            targetMethodName = inspector.targetMethodName;
        } else {
            targetClassName = targetMethod.getDeclaringClass().getCanonicalName();
            targetMethodName = targetMethod.getName();
        }
        return new ControllerProxy(controller, action, targetMethod, targetClassName, targetMethodName);
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
