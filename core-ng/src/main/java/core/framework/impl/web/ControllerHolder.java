package core.framework.impl.web;

import core.framework.api.web.Controller;

import java.lang.reflect.Method;

/**
 * @author neo
 */
public class ControllerHolder {
    public final Controller controller;
    public final Method targetMethod;
    public final String controllerInfo;

    boolean internal;
    public String action;

    public ControllerHolder(Controller controller, Method targetMethod) {
        this.controller = controller;

        if (targetMethod == null) {
            ControllerInspector inspector = new ControllerInspector(controller);
            this.targetMethod = inspector.targetMethod;
            controllerInfo = inspector.targetClassName + "." + inspector.targetMethodName;
        } else {
            this.targetMethod = targetMethod;
            controllerInfo = targetMethod.getDeclaringClass().getCanonicalName() + "." + targetMethod.getName();
        }
    }

    public ControllerHolder internal() {
        internal = true;
        return this;
    }
}
