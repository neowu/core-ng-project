package core.framework.test;

import core.framework.api.AbstractTestModule;
import core.framework.api.util.Exceptions;

/**
 * @author neo
 */
public final class TestManager {
    private static final TestManager INSTANCE = new TestManager();

    static TestManager get() {
        return INSTANCE;
    }

    private volatile AbstractTestModule testContext;
    private volatile boolean initialized;

    public synchronized void init(Class<?> testClass) {
        if (initialized) {
            if (testContext == null) {
                throw new Error("text context failed to initialize, please check error message from previous integration test");
            }
        } else {
            initialized = true;
            testContext = initializeTestContext(testClass);
        }
    }

    public Object createTest(Class<?> testClass) {
        return testContext.create(testClass);
    }

    private AbstractTestModule initializeTestContext(Class<?> testClass) {
        Context context = findContext(testClass);
        try {
            AbstractTestModule module = context.module().newInstance();
            module.configure();
            return module;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new Error("failed to create test context", e);
        }
    }

    private Context findContext(Class<?> testClass) {
        Class<?> currentClass = testClass;
        while (!currentClass.equals(Object.class)) {
            Context context = currentClass.getDeclaredAnnotation(Context.class);
            if (context != null) return context;
            currentClass = currentClass.getSuperclass();
        }
        throw Exceptions.error("integration test must have @Context(module=), testClass={}", testClass.getCanonicalName());
    }
}
