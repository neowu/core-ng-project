package core.framework.impl.web;

import core.framework.util.Exceptions;
import core.framework.web.Controller;
import core.framework.web.Request;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

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
        validateJavaVersion();

        try {
            CLASS_GET_CONSTANT_POOL = Class.class.getDeclaredMethod("getConstantPool");
            overrideAccessible(CLASS_GET_CONSTANT_POOL);

            Class<?> constantPoolClass = Class.forName("9".equals(System.getProperty("java.version")) ? "jdk.internal.reflect.ConstantPool" : "sun.reflect.ConstantPool"); // for java 8 constantPool is sun.reflect.ConstantPool, java 9 is jdk.internal.reflect.ConstantPool
            CONSTANT_POOL_GET_SIZE = constantPoolClass.getDeclaredMethod("getSize");
            overrideAccessible(CONSTANT_POOL_GET_SIZE);
            CONSTANT_POOL_GET_MEMBER_REF_INFO_AT = constantPoolClass.getDeclaredMethod("getMemberRefInfoAt", int.class);
            overrideAccessible(CONSTANT_POOL_GET_MEMBER_REF_INFO_AT);

            CONTROLLER_EXECUTE = Controller.class.getDeclaredMethod("execute", Request.class);
        } catch (ReflectiveOperationException | PrivilegedActionException e) {
            throw new Error("failed to initialize controller inspector, please contact arch team", e);
        }
    }

    private static void validateJavaVersion() {
        String javaVersion = System.getProperty("java.version");
        if ("9".equals(javaVersion)) return;

        if (!javaVersion.startsWith("1.8.0_"))
            throw Exceptions.error("unsupported java version, please use latest jdk 8, jdk={}", javaVersion);
        int minorVersion = Integer.parseInt(javaVersion.substring(6));
        if (minorVersion < 60) {
            throw Exceptions.error("unsupported java 8 version, please use latest jdk 8, jdk={}", javaVersion);
        }
    }

    private static void overrideAccessible(Method method) throws PrivilegedActionException, ReflectiveOperationException {
        Field overrideField = AccessibleObject.class.getDeclaredField("override");
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Object unsafe = AccessController.doPrivileged((PrivilegedExceptionAction<?>) () -> {
            Field field = unsafeClass.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return field.get(null);
        });
        long overrideFieldOffset = (long) unsafeClass.getMethod("objectFieldOffset", Field.class).invoke(unsafe, overrideField);
        unsafeClass.getMethod("putBoolean", Object.class, long.class, boolean.class).invoke(unsafe, method, overrideFieldOffset, true);
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
        } catch (ReflectiveOperationException e) {
            throw new Error("failed to inspect controller", e);
        }
    }
}
