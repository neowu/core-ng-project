package core.framework.internal.web.controller;

import core.framework.module.LambdaController;
import core.framework.web.Controller;
import core.framework.web.Request;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;

/**
 * due to Java 9+ doesn't provide normal way to reflect lambda method reference, here to use hack to retrieve referred method
 *
 * @author neo
 */
public class ControllerInspector {
    public final Class<?> targetClass;
    public final Method targetMethod;
    public final String controllerInfo;

    public ControllerInspector(Controller controller) {
        Class<?> controllerClass = controller.getClass();

        try {
            if (controller instanceof LambdaController) {
                Method writeReplace = controllerClass.getDeclaredMethod("writeReplace");
                if (!writeReplace.trySetAccessible()) {
                    throw new Error("failed to inspect controller, cannot access writeReplace");
                }
                SerializedLambda lambda = (SerializedLambda) writeReplace.invoke(controller);
                Class<?> targetClass = Class.forName(lambda.getImplClass().replace('/', '.'));
                String targetMethodName = lambda.getImplMethodName();
                controllerInfo = targetClass.getCanonicalName() + "." + targetMethodName;
                if (targetMethodName.contains("$")) {   // for lambda
                    this.targetClass = controllerClass;
                    targetMethod = controllerClass.getMethod("execute", Request.class);
                } else {    // for method reference
                    this.targetClass = targetClass;
                    targetMethod = targetClass.getDeclaredMethod(targetMethodName, Request.class);
                }
            } else {
                targetClass = controllerClass;
                targetMethod = controllerClass.getMethod("execute", Request.class);
                controllerInfo = controllerClass.getCanonicalName() + ".execute";
            }
        } catch (ReflectiveOperationException e) {
            throw new Error("failed to inspect controller", e);
        }
    }
}
