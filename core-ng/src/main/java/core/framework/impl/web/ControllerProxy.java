package core.framework.impl.web;

import core.framework.api.web.Controller;

import java.lang.reflect.Method;

/**
 * @author neo
 */
public class ControllerProxy {
    public final Controller controller;
    public final String action;
    public final Method targetMethod;
    public final String targetClassName;
    public final String targetMethodName;

    public ControllerProxy(Controller controller, String action, Method targetMethod, String targetClassName, String targetMethodName) {
        this.controller = controller;
        this.action = action;
        this.targetMethod = targetMethod;
        this.targetClassName = targetClassName;
        this.targetMethodName = targetMethodName;
    }
}
