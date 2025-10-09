package core.framework.test;

import core.framework.test.module.AbstractTestModule;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author neo
 */
public final class IntegrationExtension implements TestInstancePostProcessor {
    private static final ReentrantLock LOCK = new ReentrantLock();

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
        ExtensionContext.Store store = context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL);
        Class<?> testClass = context.getRequiredTestClass();
        AbstractTestModule module = getTestModule(store, testClass);
        module.inject(testInstance);
    }

    private AbstractTestModule getTestModule(ExtensionContext.Store store, Class<?> testClass) {
        try {
            LOCK.lock();
            AbstractTestModule module = store.get(AbstractTestModule.class, AbstractTestModule.class);
            if (module == null) {
                if (Boolean.TRUE.equals(store.get("initialized", Boolean.class))) {
                    throw new Error("test context failed to initialize, please check error message from previous integration test");
                }
                store.put("initialized", Boolean.TRUE);
                module = createTestModule(testClass);
                store.put(AbstractTestModule.class, module);
            }
            return module;
        } finally {
            LOCK.unlock();
        }
    }

    private AbstractTestModule createTestModule(Class<?> testClass) {
        Context context = findContext(testClass);
        try {
            AbstractTestModule module = context.module().getConstructor().newInstance();
            module.configure();
            return module;
        } catch (Exception e) {
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
        throw new Error("integration test must have @Context, testClass=" + testClass.getCanonicalName());
    }
}
