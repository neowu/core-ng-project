package core.framework.test;

import core.framework.test.inject.TestBeanFactory;
import core.framework.test.module.AbstractTestModule;
import core.framework.util.Exceptions;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

/**
 * @author neo
 */
public final class IntegrationExtension implements TestInstancePostProcessor {
    private static final String KEY_INITIALIZED = "initialized";

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
        ExtensionContext.Store store = context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL);
        Class<?> testClass = context.getRequiredTestClass();
        TestBeanFactory beanFactory = store.getOrComputeIfAbsent(TestBeanFactory.class, key -> createTestBeanFactory(testClass, store), TestBeanFactory.class);
        beanFactory.inject(testInstance);
    }

    private TestBeanFactory createTestBeanFactory(Class<?> testClass, ExtensionContext.Store store) {
        Boolean initialized = store.get(KEY_INITIALIZED, Boolean.class);
        if (Boolean.TRUE.equals(initialized)) throw new Error("test context failed to initialize, please check error message from previous integration test");
        store.put(KEY_INITIALIZED, true);
        Context context = findContext(testClass);
        TestBeanFactory beanFactory = new TestBeanFactory();
        try {
            AbstractTestModule module = context.module().getConstructor().newInstance();
            module.configure(beanFactory);
        } catch (ReflectiveOperationException e) {
            throw new Error("failed to create test context", e);
        }
        return beanFactory;
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
