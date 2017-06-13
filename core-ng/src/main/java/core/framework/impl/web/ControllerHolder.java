package core.framework.impl.web;

import core.framework.api.web.Controller;

import java.lang.reflect.Method;

/**
 * @author neo
 */
public class ControllerHolder {
    final Controller controller;
    final Method targetMethod;      // targetMethod is used to find associated annotation
    final String controllerInfo;
    final boolean skipInterceptor;
    public String action;

    public ControllerHolder(Controller controller, Method targetMethod, String controllerInfo, boolean skipInterceptor) {
        this.controller = controller;
        this.targetMethod = targetMethod;
        this.controllerInfo = controllerInfo;
        this.skipInterceptor = skipInterceptor;
    }
}
