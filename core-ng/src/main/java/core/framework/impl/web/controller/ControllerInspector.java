package core.framework.impl.web.controller;

import core.framework.web.Controller;
import core.framework.web.Request;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * due to Java 9+ doesn't provide formal way to reflect lambda method reference, we uses internal API for now
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
            overrideAccessible(CLASS_GET_CONSTANT_POOL);

            Class<?> constantPoolClass = Class.forName("jdk.internal.reflect.ConstantPool");
            CONSTANT_POOL_GET_SIZE = constantPoolClass.getDeclaredMethod("getSize");
            overrideAccessible(CONSTANT_POOL_GET_SIZE);
            CONSTANT_POOL_GET_MEMBER_REF_INFO_AT = constantPoolClass.getDeclaredMethod("getMemberRefInfoAt", int.class);
            overrideAccessible(CONSTANT_POOL_GET_MEMBER_REF_INFO_AT);

            CONTROLLER_EXECUTE = Controller.class.getDeclaredMethod("execute", Request.class);
        } catch (ReflectiveOperationException e) {
            throw new Error("failed to initialize controller inspector", e);
        }
    }

    private static void overrideAccessible(Method method) throws ReflectiveOperationException {
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field field = unsafeClass.getDeclaredField("theUnsafe");
        if (field.trySetAccessible()) {
            Object unsafe = field.get(null);
            Field overrideField = AccessibleObject.class.getDeclaredField("override");
            long overrideFieldOffset = (long) unsafeClass.getMethod("objectFieldOffset", Field.class).invoke(unsafe, overrideField);
            unsafeClass.getMethod("putBoolean", Object.class, long.class, boolean.class).invoke(unsafe, method, overrideFieldOffset, Boolean.TRUE);
        } else {
            throw new Error("failed to get unsafe");
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
                    targetMethod = targetClass.getDeclaredMethod(targetMethodName, CONTROLLER_EXECUTE.getParameterTypes());
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new Error("failed to inspect controller", e);
        }
    }
}
