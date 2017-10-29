package core.framework.test;

import core.framework.test.inject.TestBeanFactory;
import core.framework.test.module.AbstractTestModule;

import java.lang.reflect.InvocationTargetException;

/**
 * @author neo
 */
final class TestManager {
    private final TestBeanFactory beanFactory;

    TestManager(Class<? extends AbstractTestModule> moduleClass) {
        beanFactory = new TestBeanFactory();
        initializeTestModule(moduleClass);
    }

    void injectTest(Object testInstance) {
        try {
            beanFactory.inject(testInstance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new Error(e);
        }
    }

    private void initializeTestModule(Class<? extends AbstractTestModule> moduleClass) {
        try {
            AbstractTestModule module = moduleClass.getConstructor().newInstance();
            module.configure(beanFactory);
        } catch (ReflectiveOperationException e) {
            throw new Error("failed to create test context", e);
        }
    }
}
