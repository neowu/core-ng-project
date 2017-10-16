package core.framework.test;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author neo
 */
@ExtendWith(IntegrationExtension.class)
@Context(module = TestModule.class)
public abstract class IntegrationTest {
}
