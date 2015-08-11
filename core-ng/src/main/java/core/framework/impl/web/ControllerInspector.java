package core.framework.impl.web;

import core.framework.api.web.Controller;
import core.framework.api.web.Request;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * due to Java 8 doesn't provide formal way to reflect lambda method reference, we uses sun internal API for now
 * and wait for JDK update in future
 *
 * @author neo
 */
class ControllerInspector {
    static final Method GET_CONSTANT_POOL;
    static final Method CONTROLLER_METHOD;

    static {
        try {
            GET_CONSTANT_POOL = Class.class.getDeclaredMethod("getConstantPool");
            AccessController.doPrivileged((PrivilegedAction<Method>) () -> {
                GET_CONSTANT_POOL.setAccessible(true);
                return GET_CONSTANT_POOL;
            });
            CONTROLLER_METHOD = Controller.class.getDeclaredMethod("execute", Request.class);
        } catch (NoSuchMethodException e) {
            throw new Error("failed to initialize controller inspector, please contact arch team", e);
        }
    }

    final String targetClassName;
    final String targetMethodName;
    final Method targetMethod;

    ControllerInspector(Controller controller) {
        Class<?> controllerClass = controller.getClass();

        try {
            if (!controllerClass.isSynthetic()) {
                targetMethod = controllerClass.getMethod(CONTROLLER_METHOD.getName(), CONTROLLER_METHOD.getParameterTypes());
                targetClassName = controllerClass.getCanonicalName();
                targetMethodName = CONTROLLER_METHOD.getName();
            } else {
                Object constantPool = GET_CONSTANT_POOL.invoke(controllerClass); // constantPool is sun.reflect.ConstantPool, it can be changed in future JDK
                Method getSize = constantPool.getClass().getMethod("getSize");
                int size = (int) getSize.invoke(constantPool);
                Method getMemberRefInfoAt = constantPool.getClass().getMethod("getMemberRefInfoAt", int.class);
                String[] methodRefInfo = (String[]) getMemberRefInfoAt.invoke(constantPool, size - 2);
                Class<?> targetClass = Class.forName(methodRefInfo[0].replaceAll("/", "."));
                targetClassName = targetClass.getCanonicalName();
                targetMethodName = methodRefInfo[1];
                if (targetMethodName.contains("$")) {
                    targetMethod = controllerClass.getMethod(CONTROLLER_METHOD.getName(), CONTROLLER_METHOD.getParameterTypes());
                } else {
                    targetMethod = targetClass.getMethod(targetMethodName, CONTROLLER_METHOD.getParameterTypes());
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
            throw new Error("failed to inspect controller, please contact arch team", e);
        }
    }
}
