package app;

import core.framework.test.Context;
import core.framework.test.IntegrationTestRunner;
import org.junit.runner.RunWith;

/**
 * @author neo
 */
@RunWith(IntegrationTestRunner.class)
@Context(module = TestModule.class)
public abstract class IntegrationTest {

}
