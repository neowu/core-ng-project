package core.framework.test;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * @author neo
 */
public final class IntegrationTestRunner extends BlockJUnit4ClassRunner {
    public IntegrationTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        TestManager.get().init(testClass);
    }

    @Override
    protected Object createTest() throws Exception {
        return TestManager.get().createTest(getTestClass().getJavaClass());
    }
}
