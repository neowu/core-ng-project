package core.framework.test;

import core.framework.test.module.AbstractTestModule;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author neo
 */
public final class IntegrationExtension implements TestInstancePostProcessor {
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
        ExtensionContext.Store store = context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL);
        Class<?> testClass = context.getRequiredTestClass();
        AbstractTestModule module = store.computeIfAbsent(AbstractTestModule.class, _ -> createTestModule(testClass), AbstractTestModule.class);
        module.inject(testInstance);
    }

    private AbstractTestModule createTestModule(Class<?> testClass) {
        boolean initialized = INITIALIZED.get();    // junit 6 store doesn't support nested update, use global atomic to check loosely in test env
        if (initialized) throw new Error("test context failed to initialize, please check error message from previous integration test");
        INITIALIZED.set(true);

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
