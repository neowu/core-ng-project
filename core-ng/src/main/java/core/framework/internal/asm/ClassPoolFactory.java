package core.framework.internal.asm;

import javassist.ClassPool;

/**
 * @author neo
 */
public class ClassPoolFactory {
    private static ClassPool pool;

    static {
        pool = new ClassPool(null);
        pool.appendSystemPath();
    }

    public static ClassPool get() {
        return pool;
    }

    public static void cleanup() {
        pool = null;
    }
}
