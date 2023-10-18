package core.framework.module;

import core.framework.async.Executor;
import core.framework.internal.log.ActionLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class AppTest {
    private TestApp app;

    @BeforeEach
    void createApp() {
        app = new TestApp();
    }

    @Test
    void logContext() {
        ActionLog actionLog = new ActionLog(null, null);
        app.logContext(actionLog);

        assertThat(actionLog.action).isEqualTo("app:start");
    }

    @Test
    void configure() {
        app.configure();

        Executor executor = app.bean(Executor.class);
        assertThat(executor).isNotNull();
    }

    public static class TestApp extends App {
        @Override
        protected void initialize() {
        }
    }
}
