package core.framework.impl.web;

import core.framework.api.util.Exceptions;
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
public class ControllerInspector {
    private static final Method CLASS_GET_CONSTANT_POOL;
    private static final Method CONSTANT_POOL_GET_SIZE;
    private static final Method CONSTANT_POOL_GET_MEMBER_REF_INFO_AT;
    private static final Method CONTROLLER_EXECUTE;

    static {
        try {
            CLASS_GET_CONSTANT_POOL = Class.class.getDeclaredMethod("getConstantPool");
            AccessController.doPrivileged((PrivilegedAction<Method>) () -> {
                CLASS_GET_CONSTANT_POOL.setAccessible(true);
                return CLASS_GET_CONSTANT_POOL;
            });
            Class<?> constantPoolClass = Class.forName("sun.reflect.ConstantPool"); // for java 8 constantPool is sun.reflect.ConstantPool, java 9 is jdk.internal.reflect.ConstantPool
            CONSTANT_POOL_GET_SIZE = constantPoolClass.getDeclaredMethod("getSize");
            CONSTANT_POOL_GET_MEMBER_REF_INFO_AT = constantPoolClass.getDeclaredMethod("getMemberRefInfoAt", int.class);

            CONTROLLER_EXECUTE = Controller.class.getDeclaredMethod("execute", Request.class);
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            throw new Error("failed to initialize controller inspector, please contact arch team", e);
        }

        validateJavaVersion();
    }

    private static void validateJavaVersion() {
        String javaVersion = System.getProperty("java.version");
        if (!javaVersion.startsWith("1.8.0_"))
            throw Exceptions.error("unsupported java version, please use latest jdk 8, jdk={}", javaVersion);
        int minorVersion = Integer.parseInt(javaVersion.substring(6));
        if (minorVersion < 60) {
            throw Exceptions.error("unsupported java 8 version, please use latest jdk 8, jdk={}", javaVersion);
        }
    }

    public final Class<?> targetClass;
    public final Method targetMethod;
    public final String controllerInfo;

    public ControllerInspector(Controller controller) {
        Class<?> controllerClass = controller.getClass();

        try {
            if (!controllerClass.isSynthetic()) {
                targetClass = controllerClass;
                targetMethod = controllerClass.getMethod(CONTROLLER_EXECUTE.getName(), CONTROLLER_EXECUTE.getParameterTypes());
                controllerInfo = controllerClass.getCanonicalName() + "." + CONTROLLER_EXECUTE.getName();
            } else {
                Object constantPool = CLASS_GET_CONSTANT_POOL.invoke(controllerClass);
                int size = (int) CONSTANT_POOL_GET_SIZE.invoke(constantPool);
                String[] methodRefInfo = (String[]) CONSTANT_POOL_GET_MEMBER_REF_INFO_AT.invoke(constantPool, size - 3);
                Class<?> targetClass = Class.forName(methodRefInfo[0].replace('/', '.'));
                String targetMethodName = methodRefInfo[1];
                controllerInfo = targetClass.getCanonicalName() + "." + targetMethodName;
                if (targetMethodName.contains("$")) {   // for lambda
                    this.targetClass = controllerClass;
                    targetMethod = controllerClass.getMethod(CONTROLLER_EXECUTE.getName(), CONTROLLER_EXECUTE.getParameterTypes());
                } else {    // for method reference
                    this.targetClass = targetClass;
                    targetMethod = targetClass.getMethod(targetMethodName, CONTROLLER_EXECUTE.getParameterTypes());
                }
            }
        } catch (NoSuchMethodException | InvocationTargetException | ClassNotFoundException | IllegalAccessException e) {
            throw new Error("failed to inspect controller", e);
        }
    }
}
