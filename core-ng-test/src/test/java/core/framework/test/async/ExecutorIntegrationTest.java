package core.framework.test.async;

import core.framework.async.Executor;
import core.framework.inject.Inject;
import core.framework.test.IntegrationTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExecutorIntegrationTest extends IntegrationTest {
    @Inject
    Executor executor;

    @Test
    void instanceOf() {
        assertThat(executor).isInstanceOf(MockExecutor.class);
    }
}
