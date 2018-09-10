package core.framework.test;

import core.framework.test.module.AbstractTestModule;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public final class IntegrationExtension implements TestInstancePostProcessor {
    private static final String KEY_INITIALIZED = "initialized";

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
        ExtensionContext.Store store = context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL);
        Class<?> testClass = context.getRequiredTestClass();
        AbstractTestModule module = store.getOrComputeIfAbsent(AbstractTestModule.class, key -> createTestModule(testClass, store), AbstractTestModule.class);
        module.inject(testInstance);
    }

    private AbstractTestModule createTestModule(Class<?> testClass, ExtensionContext.Store store) {
        Boolean initialized = store.get(KEY_INITIALIZED, Boolean.class);
        if (Boolean.TRUE.equals(initialized)) throw new Error("test context failed to initialize, please check error message from previous integration test");
        store.put(KEY_INITIALIZED, Boolean.TRUE);
        Context context = findContext(testClass);
        try {
            AbstractTestModule module = context.module().getConstructor().newInstance();
            module.configure();
            return module;
        } catch (ReflectiveOperationException e) {
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
        throw new Error(format("integration test must have @Context, testClass={}", testClass.getCanonicalName()));
    }
}
