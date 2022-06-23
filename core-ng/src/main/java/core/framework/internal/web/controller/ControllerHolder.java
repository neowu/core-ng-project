package core.framework.internal.web.controller;

import core.framework.log.IOWarning;
import core.framework.web.Controller;

import java.lang.reflect.Method;

/**
 * @author neo
 */
public class ControllerHolder {
    public final String controllerInfo;
    public final Controller controller;
    public final String action;
    public final IOWarning[] warnings;

    final Method targetMethod;      // targetMethod is used to find associated annotation
    final boolean skipInterceptor;

    public ControllerHolder(Controller controller, Method targetMethod, String controllerInfo, String action, boolean skipInterceptor) {
        this.controller = controller;
        this.targetMethod = targetMethod;
        this.controllerInfo = controllerInfo;
        this.action = action;
        this.skipInterceptor = skipInterceptor;
        warnings = targetMethod.getDeclaredAnnotationsByType(IOWarning.class);
    }
}
