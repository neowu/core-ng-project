package core.framework.module;

import core.framework.internal.log.LogManager;
import core.framework.internal.module.ModuleContext;
import core.framework.log.LogAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * @author neo
 */
class LogConfigTest {
    private LogConfig config;

    @BeforeEach
    void createLogConfig() {
        config = new LogConfig();
        config.initialize(new ModuleContext(new LogManager()), null);
    }

    @Test
    void appender() {
        config.appendToConsole();

        assertThatThrownBy(() -> config.appender(mock(LogAppender.class)))
            .isInstanceOf(Error.class)
            .hasMessageContaining("log appender is already set");
    }
}
