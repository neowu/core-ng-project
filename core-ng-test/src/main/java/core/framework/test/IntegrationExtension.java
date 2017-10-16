package core.framework.test;

import core.framework.util.Exceptions;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

/**
 * @author neo
 */
public final class IntegrationExtension implements TestInstancePostProcessor {
    private static final String KEY_INITIALIZED = "initialized";

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        ExtensionContext.Store store = context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL);
        Class<?> testClass = context.getRequiredTestClass();
        TestManager testManager = store.getOrComputeIfAbsent(TestManager.class, key -> createTestManager(testClass, store), TestManager.class);
        testManager.injectTest(testInstance);
    }

    private TestManager createTestManager(Class<?> testClass, ExtensionContext.Store store) {
        Boolean initialized = store.get(KEY_INITIALIZED, Boolean.class);
        if (Boolean.TRUE.equals(initialized)) throw new Error("test context failed to initialize, please check error message from previous integration test");
        store.put(KEY_INITIALIZED, true);
        Context context = findContext(testClass);
        return new TestManager(context.module());
    }

    private Context findContext(Class<?> testClass) {
        Class<?> currentClass = testClass;
        while (!currentClass.equals(Object.class)) {
            Context context = currentClass.getDeclaredAnnotation(Context.class);
            if (context != null) return context;
            currentClass = currentClass.getSuperclass();
        }
        throw Exceptions.error("integration test must have @Context, testClass={}", testClass.getCanonicalName());
    }
}
