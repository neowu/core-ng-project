package core.framework.impl.web;

import core.framework.api.web.Controller;

import java.lang.reflect.Method;

/**
 * @author neo
 */
public class ControllerProxy {
    public final String action;
    public final Controller controller;
    public final Method targetMethod;
    public final String targetClassName;
    public final String targetMethodName;

    public ControllerProxy(String action, Controller controller) {
        this.action = action;
        this.controller = controller;
        ControllerInspector inspector = new ControllerInspector(controller);
        targetMethod = inspector.targetMethod;
        targetClassName = inspector.targetClassName;
        targetMethodName = inspector.targetMethodName;
    }
}
